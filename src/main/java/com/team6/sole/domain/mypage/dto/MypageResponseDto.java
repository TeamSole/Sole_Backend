package com.team6.sole.domain.mypage.dto;

import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.member.model.Social;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MypageResponseDto {
    private String profileImgUrl;

    private String socialId;

    private Social social;

    private String nickname;

    private String description;

    private int follower;

    private int following;

    @Builder
    public MypageResponseDto(String profileImgUrl, String socialId, Social social,
                             String nickname, String description,
                             int follower, int following) {
        this.profileImgUrl = profileImgUrl;
        this.socialId = socialId;
        this.social = social;
        this.nickname = nickname;
        this.description = description;
        this.follower = follower;
        this.following = following;
    }

    public static MypageResponseDto of(Member member) {
        return MypageResponseDto.builder()
                .profileImgUrl(member.getProfileImgUrl())
                .socialId(member.getSocialId())
                .social(member.getSocial())
                .nickname(member.getNickname())
                .description(member.getDescription())
                .follower(member.getFollowInfo().getFollower())
                .following(member.getFollowInfo().getFollowing())
                .build();
    }
}
