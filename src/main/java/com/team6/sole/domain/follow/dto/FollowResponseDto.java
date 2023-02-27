package com.team6.sole.domain.follow.dto;

import com.team6.sole.domain.follow.FollowRepository;
import com.team6.sole.domain.follow.entity.Follow;
import com.team6.sole.domain.member.entity.Member;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowResponseDto {
    private Long followId;

    private Member toMember;

    private int followerCount;

    private int followingCount;

    @Builder
    public FollowResponseDto(Long followId, Member toMember,
                             int followerCount, int followingCount) {
        this.followId = followId;
        this.toMember = toMember;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }

    public static FollowResponseDto of(Follow follow) {
        return FollowResponseDto.builder()
                .followId(follow.getFollowId())
                .toMember(follow.getToMember())

                .build();
    }
}
