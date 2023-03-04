package com.team6.sole.global.config.security.oauth.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppleResponseToken {
    private String access_token;
    private String expires_in;
    private String id_token;
    private String refresh_token;
    private String token_type;
    private String error;
}
