package com.team6.sole.domain.follow.dto;

import com.team6.sole.domain.follow.model.FollowStatus;
import com.team6.sole.domain.home.dto.HomeResponseDto;
import com.team6.sole.domain.member.entity.Member;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowInfoResponseDto {
    private String profileImg;

    private String nickname;

    private FollowStatus followStatus;

    private int followerCount;

    private int followingCount;

    private String description;

    private HomeResponseDto popularCourse;

    private List<HomeResponseDto> recentCourses;

    @Builder
    public FollowInfoResponseDto(String profileImg, String nickname,
                                 FollowStatus followStatus, int followerCount, int followingCount,
                                 String description, HomeResponseDto popularCourse, List<HomeResponseDto> recentCourses) {
        this.profileImg = profileImg;
        this.nickname = nickname;
        this.followStatus = followStatus;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
        this.description = description;
        this.popularCourse = popularCourse;
        this.recentCourses = recentCourses;
    }

    public static FollowInfoResponseDto of(Member toMember) {
        return FollowInfoResponseDto.builder()
            .profileImg(toMember.getProfileImgUrl())
            .nickname(toMember.getNickname())
            .followStatus(FollowStatus.FOLLOWING)
            .followerCount(toMember.getFollowInfo().getFollower())
            .followingCount(toMember.getFollowInfo().getFollowing())
            .description(toMember.getDescription())
            .build();
    }
}
