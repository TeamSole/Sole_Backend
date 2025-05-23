package com.team6.sole.domain.home.dto;

import com.team6.sole.domain.home.HomeService;
import com.team6.sole.domain.home.entity.Course;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import static com.team6.sole.domain.home.utils.RegionUtils.makeShortenAddress;

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

    private double distance;

    private Set<?> categories = new HashSet<>();

    private boolean finalPage;

    @Builder
    public HomeResponseDto(Long courseId, String thumbnailImg, String title,
                           boolean isLike, String address, int duration, double distance,
                           Set<?> categories, boolean finalPage) {
        this.courseId = courseId;
        this.thumbnailImg = thumbnailImg;
        this.title = title;
        this.isLike = isLike;
        this.address = address;
        this.duration = duration;
        this.distance = distance;
        this.categories = categories;
        this.finalPage = finalPage;
    }

    public static HomeResponseDto of(Course course, boolean isLike, boolean finalPage) {
        Set<Object> mergedSet = new HashSet<>();
        mergedSet.addAll(course.getPlaceCategories());
        mergedSet.addAll(course.getWithCategories());
        mergedSet.addAll(course.getTransCategories());

        return HomeResponseDto.builder()
                .courseId(course.getCourseId())
                .thumbnailImg(course.getThumbnailUrl())
                .title(course.getTitle())
                .isLike(isLike)
                .address(makeShortenAddress(course.getPlaces().get(0).getGps().getAddress()))
                .duration(course.getDuration())
                .distance(course.getDistance())
                .categories(mergedSet)
                .finalPage(finalPage)
                .build();
    }
}
