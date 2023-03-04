package com.team6.sole.domain.follow.dto;

import com.team6.sole.domain.follow.entity.Follow;
import com.team6.sole.domain.member.dto.MemberResponseDto;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL) //NULL 필드 가림
public class FollowResponseDto {
    private Long followId;

    private MemberResponseDto toMember;

    private int followerCount;

    private int followingCount;

    @Builder
    public FollowResponseDto(Long followId, MemberResponseDto toMember,
                             int followerCount, int followingCount) {
        this.followId = followId;
        this.toMember = toMember;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }

    public static FollowResponseDto of(Follow follow) {
        return FollowResponseDto.builder()
                .followId(follow.getFollowId())
                .toMember(MemberResponseDto.of(follow.getToMember()))
                .followerCount(follow.getToMember().getFollowInfo().getFollower())
                .followingCount(follow.getToMember().getFollowInfo().getFollowing())
                .build();
    }
}
