package com.team6.sole.domain.follow.dto;

import com.team6.sole.domain.follow.entity.Follow;
import com.team6.sole.domain.follow.model.FollowStatus;
import com.team6.sole.domain.member.dto.MemberResponseDto;
import com.team6.sole.domain.member.entity.Member;

import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL) //NULL 필드 가림
public class FollowResponseDto {
    private Long followId;

    private MemberResponseDto member;

    private FollowStatus followStatus;

    private int followerCount;

    private int followingCount;

    @Builder
    public FollowResponseDto(Long followId, MemberResponseDto member, FollowStatus followStatus,
                             int followerCount, int followingCount) {
        this.followId = followId;
        this.member = member;
        this.followStatus = followStatus;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }

    public static FollowResponseDto of(Follow follow) {
        return FollowResponseDto.builder()
                .followId(follow.getFollowId())
                .member(MemberResponseDto.of(follow.getToMember()))
                .followStatus(FollowStatus.FOLLOWING)
                .followerCount(follow.getToMember().getFollowInfo().getFollower())
                .followingCount(follow.getToMember().getFollowInfo().getFollowing())
                .build();
    }

    public static FollowResponseDto of(Follow follow, FollowStatus followStatus) {
        return FollowResponseDto.builder()
                .followId(follow.getFollowId())
                .member(MemberResponseDto.of(follow.getFromMember()))
                .followStatus(followStatus)
                .followerCount(follow.getFromMember().getFollowInfo().getFollower())
                .followingCount(follow.getFromMember().getFollowInfo().getFollowing())
                .build();
    }

    public static List<FollowResponseDto> of(List<Follow> followings) {
        return followings.stream()
                .map(FollowResponseDto::of)
                .collect(Collectors.toList());
    }

    public static Follow followToEntity(Member fromMember, Member toMember) {
        return Follow.builder()
            .fromMember(fromMember)
            .toMember(toMember)
            .build();
    }
}
