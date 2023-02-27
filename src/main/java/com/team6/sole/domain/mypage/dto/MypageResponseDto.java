package com.team6.sole.domain.mypage.dto;

import com.team6.sole.domain.member.entity.Member;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MypageResponseDto {
    private String profileImgUrl;

    private String socialId;

    private String nickname;

    private String description;

    @Builder
    public MypageResponseDto(String profileImgUrl, String socialId,
                             String nickname, String description) {
        this.profileImgUrl = profileImgUrl;
        this.socialId = socialId;
        this.nickname = nickname;
        this.description = description;
    }

    public static MypageResponseDto of(Member member) {
        return MypageResponseDto.builder()
                .profileImgUrl(member.getProfileImgUrl())
                .socialId(member.getSocialId())
                .nickname(member.getNickname())
                .description(member.getDescription())
                .build();
    }
}
