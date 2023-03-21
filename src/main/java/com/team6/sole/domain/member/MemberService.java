package com.team6.sole.domain.member;

import com.team6.sole.domain.home.entity.Category;
import com.team6.sole.domain.home.entity.Gps;
import com.team6.sole.domain.member.dto.*;
import com.team6.sole.domain.member.entity.Accept;
import com.team6.sole.domain.member.entity.FollowInfo;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.member.entity.NotificationInfo;
import com.team6.sole.domain.member.model.Role;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
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

    // 회원체크 및 로그인(소셜)
    @SneakyThrows // 명시적 예외처리(lombok)
    @Transactional
    public ResponseEntity<CommonApiResponse<MemberResponseDto>> checkMember(String provider, OauthRequest oauthRequest) {
        String socialId = "";
        Social social = null;

        if (provider.equals("kakao")) {
            socialId = getKakaoUser(oauthRequest.getAccessToken()).getAuthenticationCode();
            log.info(socialId);
            social = Social.KAKAO;
        } else {
            socialId = appleUtils.verifyPublicKey(oauthRequest.getAccessToken()).get("sub").toString();
            social = Social.APPLE;
        }
        Optional<Member> checkMember = memberRepository.findBySocialIdAndSocial(socialId, social);

        if (checkMember.isPresent()) {
            HttpHeaders httpHeaders = new HttpHeaders();
            TokenResponseDto tokenResponseDTO = tokenProvider.generateToken(socialId, checkMember.get().getRole().name());
            httpHeaders.add("Authorization", "Bearer " + tokenResponseDTO.getAccessToken());
            checkMember.get().setFcmToken(
                    oauthRequest.getFcmToken() == null
                            ? null
                            : oauthRequest.getFcmToken()
            );

            log.info("로그인 성공");

            return new ResponseEntity<>(CommonApiResponse.of(MemberResponseDto.ofCheck(checkMember.get(), true, tokenResponseDTO)), httpHeaders, HttpStatus.OK);
        } else {
            return ResponseEntity.ok(CommonApiResponse.of(MemberResponseDto.ofSignUp(false)));
        }
    }

    // 회원가입(소셜)
    @SneakyThrows // 명시적 예외처리(lombok)
    @Transactional
    public ResponseEntity<CommonApiResponse<MemberResponseDto>> makeMember(String provider, MultipartFile multipartFile, MemberRequestDto memberRequestDto) {
        String socialId = "";
        Social social = null;

        if (provider.equals("kakao")) {
            socialId = getKakaoUser(memberRequestDto.getAccessToken()).getAuthenticationCode();
            log.info(socialId);
            social = Social.KAKAO;
        } else {
            socialId = appleUtils.verifyPublicKey(memberRequestDto.getAccessToken()).get("sub").toString();
            log.info(socialId);
            social = Social.APPLE;
        }

        Accept accept = Accept.builder()
                .serviceAccepted(memberRequestDto.isServiceAccepted())
                .infoAccepted(memberRequestDto.isInfoAccepted())
                .marketingAccepted(memberRequestDto.isMarketingAccepted())
                .locationAccepted(memberRequestDto.isLocationAccepted())
                .build();
        acceptRepository.save(accept);

        Member member = Member.builder()
                .socialId(socialId)
                .password(passwordEncoder.encode("social"))
                .nickname(memberRequestDto.getNickname())
                .social(social)
                .role(Role.ROLE_USER)
                .profileImgUrl(
                        multipartFile == null
                                ? null
                                : awsS3Service.uploadImage(multipartFile, "member"))
                .accept(accept)
                .favoriteCategory(
                        Category.builder()
                                .placeCategories(memberRequestDto.getPlaceCategories())
                                .withCategories(memberRequestDto.getWithCategories())
                                .transCategories(memberRequestDto.getTransCategories())
                                .build()
                )
                .description(null)
                .followInfo(
                        FollowInfo.builder()
                                .follower(0)
                                .following(0)
                                .build()
                )
                .notificationInfo(
                        NotificationInfo.builder()
                                .activityNot(true)
                                .marketingNot(memberRequestDto.isMarketingAccepted())
                                .build()
                )
                .fromFollows(new ArrayList<>())
                .toFollows(new ArrayList<>())
                .fcmToken(
                        memberRequestDto.getFcmToken() == null
                                ? null
                                : memberRequestDto.getFcmToken())
                .currentGps(
                        Gps.builder()
                                .address("서울 마포구 마포대로 122")
                                .latitude(126.952499) // 위도(x)
                                .longitude(37.5453021) // 경도(y)
                                .distance(0)
                                .build()
                )
                .build();
        memberRepository.saveAndFlush(member);
        
        ScrapFolder scrapFolder = ScrapFolder.builder()
                .scrapFolderName("기본 폴더")
                .member(member)
                .build();
        scrapFolderRespository.saveAndFlush(scrapFolder);

        HttpHeaders httpHeaders = new HttpHeaders();
        TokenResponseDto tokenResponseDTO = tokenProvider.generateToken(socialId, member.getRole().name());
        httpHeaders.add("Authorization", "Bearer " + tokenResponseDTO.getAccessToken());

        log.info("회원가입 성공");

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
            log.info(refreshToken);
            log.info(redisService.getValues(socialId));
        } catch (Exception e) {
            throw new BadRequestException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        tokenProvider.validateRefreshToken(socialId, refreshToken);

        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        TokenResponseDto tokenResponseDto = tokenProvider.generateToken(socialId, member.getRole().name());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + tokenResponseDto.getAccessToken());

        return new ResponseEntity<>(CommonApiResponse.of(tokenResponseDto), httpHeaders, HttpStatus.OK);
    }
    
    // fcmToken 교체
    @Transactional
    public String modFcmToken(String socialId, FcmTokenDto fcmTokenDto) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        member.setFcmToken(fcmTokenDto.getFcmToken());

        return "fcmToken 교체 성공";
    }

    // 로그아웃(fcmToken 삭제)
    @Transactional
    public String logout(String socialId) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

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
    public ResponseEntity<CommonApiResponse<MemberResponseDto>> checkMember() {
        Member checkMember = memberRepository.findBySocialIdAndSocial("jimin1126@hanmail.net", Social.KAKAO)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        HttpHeaders httpHeaders = new HttpHeaders();
        TokenResponseDto tokenResponseDTO = tokenProvider.generateToken(checkMember.getSocialId(), checkMember.getRole().name());
        httpHeaders.add("Authorization", "Bearer " + tokenResponseDTO.getAccessToken());

        log.info("로그인 성공");

        return new ResponseEntity<>(CommonApiResponse.of(MemberResponseDto.of(checkMember, tokenResponseDTO)), httpHeaders, HttpStatus.OK);
    }

    // 카카오 유저 정보 가져오기
    public KakaoUserDto getKakaoUser(String accessToken) {
        String getUserURL = "https://kapi.kakao.com/v2/user/me";

        try {
            return webClient.post()
                    .uri(getUserURL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserDto.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(ErrorCode.KAKAO_BAD_REQUEST);
        }
    }
}
