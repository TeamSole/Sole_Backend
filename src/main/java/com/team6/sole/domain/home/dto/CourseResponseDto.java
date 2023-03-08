package com.team6.sole.domain.home.dto;

import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.member.dto.MemberResponseDto;
import lombok.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseResponseDto {
    private Long courseId;

    private String thumbnailUrl;

    private String title;

    private int scrapCount;

    private String description;

    private String startDate;

    private int duration;

    private int distance;

    private Set<?> categories = new HashSet<>();

    List<PlaceResponseDto> placeResponseDtos;

    private MemberResponseDto writer;

    @Builder
    public CourseResponseDto(Long courseId, String title, int scrapCount,
                             String description, String startDate,
                             int duration, int distance,
                             Set<?> categories, List<PlaceResponseDto> placeResponseDtos,
                             MemberResponseDto writer) {
        this.courseId = courseId;
        this.title = title;
        this.scrapCount = scrapCount;
        this.description = description;
        this.startDate = startDate;
        this.duration = duration;
        this.distance = distance;
        this.categories = categories;
        this.placeResponseDtos = placeResponseDtos;
        this.writer = writer;
    }

    public static CourseResponseDto of(Course course) {
        Set<Enum> mergedSet = new HashSet<>();
        mergedSet.addAll(course.getCategory().getPlaceCategories());
        mergedSet.addAll(course.getCategory().getWithCategories());
        mergedSet.addAll(course.getCategory().getTransCategories());

        DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

        return CourseResponseDto.builder()
                .courseId(course.getCourseId())
                .title(course.getTitle())
                .scrapCount(course.getScrapCount())
                .description(course.getDescription())
                .startDate(dateFormat.format(course.getStartDate()))
                .duration(course.getDuration())
                .distance(course.getDistance())
                .categories(mergedSet)
                .placeResponseDtos(course.getPlaces().stream()
                        .map(PlaceResponseDto::of)
                        .collect(Collectors.toList()))
                .writer(MemberResponseDto.of(course.getWriter()))
                .build();
    }
}
