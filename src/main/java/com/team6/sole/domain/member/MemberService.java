package com.team6.sole.domain.member;

import com.team6.sole.domain.member.dto.KakaoUserDto;
import com.team6.sole.domain.member.dto.MemberRequestDto;
import com.team6.sole.domain.member.dto.MemberResponseDto;
import com.team6.sole.domain.member.dto.OauthRequest;
import com.team6.sole.domain.member.entity.Accept;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.member.model.Social;
import com.team6.sole.global.config.CommonApiResponse;
import com.team6.sole.global.config.s3.AwsS3ServiceImpl;
import com.team6.sole.global.config.security.dto.TokenResponseDto;
import com.team6.sole.global.config.security.jwt.TokenProvider;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final AcceptRepository acceptRepository;
    private final AwsS3ServiceImpl awsS3Service;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final WebClient webClient;

    // 회원가입 및 로그인(소셜)
    @Transactional
    public ResponseEntity<CommonApiResponse<MemberResponseDto>> makeMember(String provider, OauthRequest oauthRequest, MemberRequestDto memberRequestDto, MultipartFile multipartFile) {
        String email = "";
        Social social = null;

        if (provider.equals("kakao")) {
            email = getKakaoUser(oauthRequest.getAccessToken()).getKakaoAccount().getEmail();
            social = Social.KAKAO;
        }
        Optional<Member> checkMember = memberRepository.findByEmailAndSocial(email, social);

        if (checkMember.isPresent()) {
            HttpHeaders httpHeaders = new HttpHeaders();
            TokenResponseDto tokenResponseDTO = tokenProvider.generateToken(email);
            httpHeaders.add("Authorization", "Bearer " + tokenResponseDTO.getAccessToken());

            return new ResponseEntity<>(CommonApiResponse.of(MemberResponseDto.of(checkMember.get(), tokenResponseDTO)), httpHeaders, HttpStatus.OK);
        } else {
            Accept accept = Accept.builder()
                    .serviceAccepted(memberRequestDto.isServiceAccepted())
                    .infoAccepted(memberRequestDto.isInfoAccepted())
                    .marketingAccepted(memberRequestDto.isMarketingAccepted())
                    .build();
            acceptRepository.save(accept);

            Member member = Member.builder()
                    .email(email)
                    .password(passwordEncoder.encode("social"))
                    .nickname(memberRequestDto.getNickname())
                    .social(social)
                    .profileImgUrl(
                            multipartFile == null
                                    ? null
                                    : awsS3Service.uploadImage(multipartFile, "member"))
                    .accept(accept)
                    .build();
            memberRepository.save(member);

            HttpHeaders httpHeaders = new HttpHeaders();
            TokenResponseDto tokenResponseDTO = tokenProvider.generateToken(email);
            httpHeaders.add("Authorization", "Bearer " + tokenResponseDTO.getAccessToken());

            return new ResponseEntity<>(CommonApiResponse.of(MemberResponseDto.of(member, tokenResponseDTO)), httpHeaders, HttpStatus.OK);
        }
    }

    // 토큰 재발급
    @Transactional
    public ResponseEntity<CommonApiResponse<TokenResponseDto>> reissue(String accessToken, String refreshToken) {
        String email;

        if (!tokenProvider.validateTokenExceptExpiration(accessToken)){
            throw new BadRequestException(ErrorCode.INVALID_ACCESS_TOKEN);
        }

        try {
            email = tokenProvider.parseClaims(accessToken).getSubject();
        } catch (Exception e) {
            throw new BadRequestException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        tokenProvider.validateRefreshToken(email, refreshToken);

        TokenResponseDto tokenResponseDto = tokenProvider.generateToken(email);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + tokenResponseDto.getAccessToken());

        return new ResponseEntity<>(CommonApiResponse.of(tokenResponseDto), httpHeaders, HttpStatus.OK);
    }

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
