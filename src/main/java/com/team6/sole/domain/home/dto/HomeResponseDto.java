package com.team6.sole.domain.home.dto;

import com.team6.sole.domain.home.entity.Course;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeResponseDto {
    private Long courseId;

    private String thumbnailImg;

    private String title;

    private boolean isLike;

    private String address;

    private int duration;

    private int distance;

    private Set<?> categories = new HashSet<>();

    @Builder
    public HomeResponseDto(Long courseId, String thumbnailImg, String title,
                           boolean isLike, String address, int duration, int distance,
                           Set<?> categories) {
        this.courseId = courseId;
        this.thumbnailImg = thumbnailImg;
        this.title = title;
        this.isLike = isLike;
        this.address = address;
        this.duration = duration;
        this.distance = distance;
        this.categories = categories;
    }

    public static HomeResponseDto of(Course course, boolean isLike) {
        Set<Object> mergedSet = new HashSet<>();
        mergedSet.addAll(course.getPlaceCategories());
        mergedSet.addAll(course.getWithCategories());
        mergedSet.addAll(course.getTransCategories());

        return HomeResponseDto.builder()
                .courseId(course.getCourseId())
                .thumbnailImg(course.getThumbnailUrl())
                .title(course.getTitle())
                .isLike(isLike)
                .address(course.getPlaces().get(0).getGps().getAddress())
                .duration(course.getDuration())
                .distance(course.getDistance())
                .categories(mergedSet)
                .build();
    }
}
