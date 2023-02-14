package com.team6.sole.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserDto {
    @JsonProperty("id")
    private String authenticationCode;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @JsonProperty("properties")
    private Properties properties;

    @Getter
    @ToString
    public static class KakaoAccount {
        private String email;
    }

    @Getter
    @ToString
    public static class Properties {
        private String nickname;
    }
}
