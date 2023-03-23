package com.team6.sole.domain.home.dto;

import com.team6.sole.domain.home.entity.Course;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecommendCourseResponseDto implements Serializable {
    private Long courseId;

    private String courseName;

    private String thumbnailImg;

    @Builder
    public RecommendCourseResponseDto(Long courseId, String courseName, String thumbnailImg) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.thumbnailImg = thumbnailImg;
    }

    public static RecommendCourseResponseDto of(Course course) {
        return RecommendCourseResponseDto.builder()
                .courseId(course.getCourseId())
                .courseName(course.getTitle())
                .thumbnailImg(course.getThumbnailUrl())
                .build();
    }
}
