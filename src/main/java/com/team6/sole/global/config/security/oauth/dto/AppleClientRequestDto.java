package com.team6.sole.global.config.security.oauth.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppleClientRequestDto {
    private String identityToken;
    private String authorizationCode;
}
