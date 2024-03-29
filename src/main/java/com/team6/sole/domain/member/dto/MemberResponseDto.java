package com.team6.sole.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.member.model.Role;
import com.team6.sole.domain.member.model.Social;
import com.team6.sole.global.config.security.dto.TokenResponseDto;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/*@JsonInclude(JsonInclude.Include.NON_NULL) //NULL 필드 가림*/
public class MemberResponseDto implements Serializable {
    private Long memberId;

    private String socialId;

    private String nickname;

    private String profileImgUrl;

    private Social social;

    private Role role;

    private String accessToken;

    private String refreshToken;

    private boolean check;

    @Builder
    public MemberResponseDto(Long memberId, String socialId, String nickname,
                             String profileImgUrl, Social social, Role role,
                             String accessToken, String refreshToken, boolean check) {
        this.memberId = memberId;
        this.socialId = socialId;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.social = social;
        this.role = role;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.check = check;
    }

    public static MemberResponseDto of(Member member) {
        return MemberResponseDto.builder()
                .memberId(member.getMemberId())
                .socialId(member.getSocialId())
                .nickname(member.getNickname())
                .profileImgUrl(member.getProfileImgUrl())
                .social(member.getSocial())
                .role(member.getRole())
                .build();
    }

    public static MemberResponseDto ofCheck(Member member, boolean check, TokenResponseDto tokenResponseDto) {
        return MemberResponseDto.builder()
                .memberId(member.getMemberId())
                .socialId(member.getSocialId())
                .nickname(member.getNickname())
                .profileImgUrl(member.getProfileImgUrl())
                .social(member.getSocial())
                .role(member.getRole())
                .accessToken(tokenResponseDto.getAccessToken())
                .refreshToken(tokenResponseDto.getRefreshToken())
                .check(check)
                .build();
    }

    public static MemberResponseDto of(Member member, TokenResponseDto tokenResponseDto) {
        return MemberResponseDto.builder()
                .memberId(member.getMemberId())
                .socialId(member.getSocialId())
                .nickname(member.getNickname())
                .profileImgUrl(member.getProfileImgUrl())
                .social(member.getSocial())
                .role(member.getRole())
                .accessToken(tokenResponseDto.getAccessToken())
                .refreshToken(tokenResponseDto.getRefreshToken())
                .build();
    }

    public static MemberResponseDto ofSignUp(boolean check) {
        return MemberResponseDto.builder()
                .check(check)
                .accessToken(null)
                .refreshToken(null)
                .build();
    }
}
