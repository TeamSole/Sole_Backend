package com.team6.sole.global.config.security.oauth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppleRequestToken {
    private String code;
    private String client_id;
    private String client_secret;
    private String grant_type;
    private String refresh_token;

    @Builder
    public AppleRequestToken(String code, String client_id, String client_secret,
                             String grant_type, String refresh_token) {
        this.code = code;
        this.client_id = client_id;
        this.client_secret = client_secret;
        this.grant_type = grant_type;
        this.refresh_token = refresh_token;
    }

    public static AppleRequestToken of(AppleRequestToken appleRequestToken) {
        return AppleRequestToken.builder()
                .code(appleRequestToken.getCode())
                .client_id(appleRequestToken.getClient_id())
                .client_secret(appleRequestToken.getClient_secret())
                .grant_type(appleRequestToken.getGrant_type())
                .refresh_token(appleRequestToken.getRefresh_token())
                .build();
    }
}
