package com.team6.sole.domain.home.dto;

import com.team6.sole.domain.follow.model.FollowStatus;
import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.member.dto.MemberResponseDto;
import com.team6.sole.domain.member.entity.Member;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseDetailResponseDto {
    private Long courseId;

    private MemberResponseDto writer;

    private boolean isWriter;

    private int follower;

    private int following;

    private FollowStatus followStatus;

    private String title;

    private int scrapCount;

    private String description;

    private String startDate;

    private int duration;

    private int distance;

    private Set<?> categories = new HashSet<>();

    private List<PlaceResponseDto> placeResponseDtos = new ArrayList<>();

    @Builder
    public CourseDetailResponseDto(Long courseId, MemberResponseDto writer, boolean isWriter, int follower, int following,
                                   FollowStatus followStatus, String title, int scrapCount, String description,
                                   String startDate, int duration, int distance,
                                   Set<?> categories, List<PlaceResponseDto> placeResponseDtos) {
        this.courseId = courseId;
        this.writer = writer;
        this.isWriter = isWriter;
        this.follower = follower;
        this.following = following;
        this.followStatus = followStatus;
        this.title = title;
        this.scrapCount = scrapCount;
        this.description = description;
        this.startDate = startDate;
        this.duration = duration;
        this.distance = distance;
        this.categories = categories;
        this.placeResponseDtos = placeResponseDtos;
    }

    public static CourseDetailResponseDto of(Course course, boolean isWriter, FollowStatus followStatus) {
        Set<Object> mergedSet = new HashSet<>();
        mergedSet.addAll(course.getPlaceCategories());
        mergedSet.addAll(course.getTransCategories());
        mergedSet.addAll(course.getWithCategories());
        return CourseDetailResponseDto.builder()
                .courseId(course.getCourseId())
                .writer(MemberResponseDto.of(course.getWriter()))
                .isWriter(isWriter)
                .follower(course.getWriter().getFollowInfo().getFollower())
                .following(course.getWriter().getFollowInfo().getFollowing())
                .followStatus(followStatus)
                .title(course.getTitle())
                .scrapCount(course.getScrapCount())
                .description(course.getDescription())
                .startDate(course.getStartDate().toString())
                .duration(course.getDuration())
                .distance(course.getDistance())
                .categories(mergedSet)
                .placeResponseDtos(course.getPlaces().stream()
                        .map(PlaceResponseDto::of)
                        .collect(Collectors.toList()))
                .build();
    }
}
