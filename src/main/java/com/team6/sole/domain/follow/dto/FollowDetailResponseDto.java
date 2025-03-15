package com.team6.sole.domain.follow.dto;

import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.member.entity.Member;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowDetailResponseDto {
    private Long courseId;

    private String profileImg;

    private String nickname;

    private boolean isLike;

    private String thumbnailImg;

    private String title;

    private String description;

    @Builder
    public FollowDetailResponseDto(Long courseId, String profileImg, String nickname,
                                   boolean isLike, String thumbnailImg, String title, String description) {
        this.courseId = courseId;
        this.profileImg = profileImg;
        this.nickname = nickname;
        this.isLike = isLike;
        this.thumbnailImg = thumbnailImg;
        this.title = title;
        this.description = description;
    }

    public static FollowDetailResponseDto of(Member toMember, boolean isLike, Course course) {
      return FollowDetailResponseDto.builder()
              .courseId(course.getCourseId())
              .profileImg(toMember.getProfileImgUrl())
              .nickname(toMember.getNickname())
              .isLike(isLike)
              .thumbnailImg(course.getThumbnailUrl())
              .title(course.getTitle())
              .description(course.getDescription())
              .build();
    }
} 
