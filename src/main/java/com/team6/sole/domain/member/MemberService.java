package com.team6.sole.domain.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.team6.sole.domain.member.dto.*;
import com.team6.sole.domain.member.entity.Accept;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.member.model.Social;
import com.team6.sole.domain.scrap.ScrapFolderRespository;
import com.team6.sole.domain.scrap.entity.ScrapFolder;
import com.team6.sole.global.config.CommonApiResponse;
import com.team6.sole.global.config.redis.RedisService;
import com.team6.sole.global.config.s3.AwsS3ServiceImpl;
import com.team6.sole.global.config.security.dto.TokenResponseDto;
import com.team6.sole.global.config.security.jwt.TokenProvider;
import com.team6.sole.global.config.security.oauth.AppleUtils;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.BadRequestException;
import com.team6.sole.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    private final ScrapFolderRespository scrapFolderRespository;
    private final MemberRepository memberRepository;
    private final AcceptRepository acceptRepository;
    private final AwsS3ServiceImpl awsS3Service;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final WebClient webClient;
    private final AppleUtils appleUtils;
    private final RedisService redisService;

    private static final String KAKAO = "kakao";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    // 회원체크 및 로그인(소셜)
    @SneakyThrows // 명시적 예외처리(lombok)
    @Transactional
    public ResponseEntity<CommonApiResponse<MemberResponseDto>> checkMember(String provider, OauthRequest oauthRequest) {
        String socialCode = getSocialCode(provider, oauthRequest.getAccessToken());
        Social social = getSocial(provider);
        Optional<Member> checkMember = memberRepository.findBySocialIdAndSocial(socialCode, social);

        if (checkMember.isPresent()) {
            TokenResponseDto tokenResponseDTO = tokenProvider.generateToken(socialCode, checkMember.get().getRole().name());
            HttpHeaders httpHeaders = makeHttpHeaders(tokenResponseDTO);

            checkMember.get().setFcmToken(checkFcmToken(oauthRequest));

            return new ResponseEntity<>(CommonApiResponse.of(MemberResponseDto.ofCheck(checkMember.get(), true, tokenResponseDTO)), httpHeaders, HttpStatus.OK);
        } else {
            return ResponseEntity.ok(CommonApiResponse.of(MemberResponseDto.ofSignUp(false)));
        }
    }

    // fcmToken null 체크
    public String checkFcmToken(OauthRequest oauthRequest) {
        return oauthRequest.getFcmToken() == null
                ? null
                : oauthRequest.getFcmToken();
    }

    // Http Header 정보 생성
    public HttpHeaders makeHttpHeaders(TokenResponseDto tokenResponseDTO) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(AUTHORIZATION, BEARER + tokenResponseDTO.getAccessToken());

        return httpHeaders;
    }

    // 소셜 판별
    public Social getSocial(String provider) {
        return provider.equals(KAKAO)
                ? Social.KAKAO
                : Social.APPLE;
    }

    // 소셜 고유번호 가져오기
    public String getSocialCode(String provider, String socialAccessToken)
            throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException, JsonProcessingException {
        String socialCode = "";

        if (provider.equals(KAKAO)) {
            socialCode = getKakaoUser(socialAccessToken).getAuthenticationCode();
        } else {
            socialCode = appleUtils.verifyPublicKey(socialAccessToken).get("sub").toString();
        }

        return socialCode;
    }

    // 회원가입(소셜)
    @SneakyThrows // 명시적 예외처리(lombok)
    @Transactional
    public ResponseEntity<CommonApiResponse<MemberResponseDto>> makeMember(String provider, MultipartFile multipartFile, MemberRequestDto memberRequestDto) {
        if (memberRepository.existsByNickname(memberRequestDto.getNickname())) {
            throw new BadRequestException(ErrorCode.MEMBER_ALREADY_EXIST);
        }

        String socialCode = getSocialCode(provider, memberRequestDto.getAccessToken());
        Social social = getSocial(provider);
        String password = passwordEncoder.encode("social");
        String profileImgUrl = multipartFile == null 
                        ? null 
                        : awsS3Service.uploadImage(multipartFile, "member");

        Accept accept = MemberRequestDto.acceptToEntity(memberRequestDto);
        acceptRepository.save(accept);

        Member member = MemberRequestDto.memberToEntity(socialCode, password, social, profileImgUrl, accept, memberRequestDto);
        memberRepository.saveAndFlush(member);

        ScrapFolder scrapFolder = MemberRequestDto.scrapFolderToEntity(member, memberRequestDto);
        scrapFolderRespository.saveAndFlush(scrapFolder);

        TokenResponseDto tokenResponseDTO = tokenProvider.generateToken(socialCode, member.getRole().name());
        HttpHeaders httpHeaders = makeHttpHeaders(tokenResponseDTO);

        return new ResponseEntity<>(CommonApiResponse.of(MemberResponseDto.of(member, tokenResponseDTO)), httpHeaders, HttpStatus.OK);
    }

    // 토큰 재발급
    @Transactional
    public ResponseEntity<CommonApiResponse<TokenResponseDto>> reissue(String accessToken, String refreshToken) {
        String socialId;

        if (!tokenProvider.validateTokenExceptExpiration(accessToken)){
            throw new BadRequestException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        try {
            socialId = tokenProvider.parseClaims(accessToken).getSubject();
        } catch (Exception e) {
            throw new BadRequestException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        tokenProvider.validateRefreshToken(socialId, refreshToken);

        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        TokenResponseDto tokenResponseDto = tokenProvider.generateToken(socialId, member.getRole().name());
        HttpHeaders httpHeaders = makeHttpHeaders(tokenResponseDto);

        return new ResponseEntity<>(CommonApiResponse.of(tokenResponseDto), httpHeaders, HttpStatus.OK);
    }
    
    // fcmToken 교체
    @Transactional
    public String modFcmToken(Member member, FcmTokenDto fcmTokenDto) {
        member.setFcmToken(fcmTokenDto.getFcmToken());

        return "fcmToken 교체 성공";
    }

    // 로그아웃(fcmToken 삭제)
    @Transactional
    public String logout(Member member) {
        /*//이미 로그아웃 된 상태
        if (StringUtils.isBlank(member.getFcmToken())) {
            throw new BadRequestException(ErrorCode.USER_ALREADY_LOGGED_OUT);
        }*/

        //redis 에서 registerToken 삭제
        tokenProvider.deleteRegisterToken(member.getSocialId());

        member.setFcmToken(null);

        return "로그아웃 성공";
    }

    // 닉네임 중복 검사
    @Transactional(readOnly = true)
    public Boolean duplicateNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }

    // 테스트 로그인
    @Transactional
    public ResponseEntity<CommonApiResponse<MemberResponseDto>> checkMember(Long memberId) {
        Member checkMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        HttpHeaders httpHeaders = new HttpHeaders();
        TokenResponseDto tokenResponseDTO = tokenProvider.generateToken(checkMember.getSocialId(), checkMember.getRole().name());
        httpHeaders.add("Authorization", "Bearer " + tokenResponseDTO.getAccessToken());

        log.info("로그인 성공");

        return new ResponseEntity<>(CommonApiResponse.of(MemberResponseDto.of(checkMember, tokenResponseDTO)), httpHeaders, HttpStatus.OK);
    }

    // 회원 삭제(중복 가입 방지용)
    @Transactional
    public String delMember(Long memberId) {
        memberRepository.deleteByMemberId(memberId);
        return "삭제 성공!";
    }

    // 카카오 유저 정보 가져오기
    public KakaoUserDto getKakaoUser(String accessToken) {
        String getUserURL = "https://kapi.kakao.com/v2/user/me";

        try {
            return webClient.post()
                    .uri(getUserURL)
                    .header(AUTHORIZATION, BEARER + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserDto.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(ErrorCode.KAKAO_BAD_REQUEST);
        }
    }
}
