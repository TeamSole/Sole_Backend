package com.team6.sole.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.member.model.Social;
import com.team6.sole.global.config.security.dto.TokenResponseDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL) //NULL 필드 가림
public class MemberResponseDto {
    private Long memberId;

    private String email;

    private String nickname;

    private String profileImgUrl;

    private Social social;

    private String accessToken;

    private String refreshToken;

    @Builder
    public MemberResponseDto(Long memberId, String email, String nickname,
                             String profileImgUrl, Social social,
                             String accessToken, String refreshToken) {
        this.memberId = memberId;
        this.email = email;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.social = social;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public static MemberResponseDto of(Member member) {
        return MemberResponseDto.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImgUrl(member.getProfileImgUrl())
                .social(member.getSocial())
                .build();
    }

    public static MemberResponseDto of(Member member, TokenResponseDto tokenResponseDto) {
        return MemberResponseDto.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImgUrl(member.getProfileImgUrl())
                .social(member.getSocial())
                .accessToken(tokenResponseDto.getAccessToken())
                .refreshToken(tokenResponseDto.getRefreshToken())
                .build();
    }
}
