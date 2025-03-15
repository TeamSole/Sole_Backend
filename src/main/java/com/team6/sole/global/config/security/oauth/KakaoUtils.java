package com.team6.sole.global.config.security.oauth;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.team6.sole.global.config.security.oauth.dto.KakaoMemberDto;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.BadRequestException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.team6.sole.global.config.security.jwt.TokenProvider.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class KakaoUtils {
    private final WebClient webClient;

    private static final String KAKAO_AUTH_URL = "https://kapi.kakao.com/v2/user/me";

    public static final String KAKAO = "kakao";
    
    // 카카오에서 유저 정보 가져오기
    public KakaoMemberDto getKakaoMember(String accessToken) {
        try {
            return webClient.post()
                    .uri(KAKAO_AUTH_URL)
                    .header(AUTHORIZATION, BEARER + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoMemberDto.class)
                    .block();
        } catch (Exception e) {
            log.info(e.getMessage());
            throw new BadRequestException(ErrorCode.KAKAO_BAD_REQUEST);
        }
    }
}