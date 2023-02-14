package com.team6.sole.global.config.security.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenResponseDto {

    private String accessToken;

    private String refreshToken;
}
