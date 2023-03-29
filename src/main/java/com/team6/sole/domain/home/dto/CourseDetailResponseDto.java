package com.team6.sole.domain.home.dto;

import com.team6.sole.domain.follow.model.FollowStatus;
import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.Gps;
import com.team6.sole.domain.member.dto.MemberResponseDto;
import com.team6.sole.domain.member.entity.Member;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.team6.sole.domain.home.HomeService.makeShortenAddress;
import static com.team6.sole.infra.direction.DirectionService.calculateDistance;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseDetailResponseDto {
    private Long courseId;

    private MemberResponseDto writer;

    private boolean checkWriter;

    private int follower;

    private int following;

    private FollowStatus followStatus;

    private String title;

    private String thumbnailUrl;

    private int scrapCount;

    private String description;

    private String startDate;

    private String address;

    private int duration;

    private double distance;

    private Set<?> categories = new HashSet<>();

    private List<PlaceResponseDto> placeResponseDtos = new ArrayList<>();

    private List<Double> durationList = new ArrayList<>();

    @Builder
    public CourseDetailResponseDto(Long courseId, MemberResponseDto writer, boolean checkWriter, int follower, int following,
                                   FollowStatus followStatus, String title, String thumbnailUrl, int scrapCount, String description,
                                   String startDate, String address, int duration, double distance,
                                   Set<?> categories, List<PlaceResponseDto> placeResponseDtos,
                                   List<Double> durationList) {
        this.courseId = courseId;
        this.writer = writer;
        this.checkWriter = checkWriter;
        this.follower = follower;
        this.following = following;
        this.followStatus = followStatus;
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.scrapCount = scrapCount;
        this.description = description;
        this.startDate = startDate;
        this.address = address;
        this.duration = duration;
        this.distance = distance;
        this.categories = categories;
        this.placeResponseDtos = placeResponseDtos;
        this.durationList = durationList;
    }

    public static CourseDetailResponseDto of(Course course, boolean checkWriter, FollowStatus followStatus) {
        Set<Object> mergedSet = new HashSet<>();
        mergedSet.addAll(course.getPlaceCategories());
        mergedSet.addAll(course.getTransCategories());
        mergedSet.addAll(course.getWithCategories());

        List<Double> durationList = new ArrayList<>();

        for (int i = 0; i < course.getPlaces().size(); i++) {
            if (i == course.getPlaces().size() - 1) {
                break;
            }
            Gps start = course.getPlaces().get(i).getGps();
            Gps end = course.getPlaces().get(i + 1).getGps();
            durationList.add(calculateDistance(start.getLatitude(), start.getLongitude(),
                    end.getLatitude(), end.getLongitude()));
        }

        return CourseDetailResponseDto.builder()
                .courseId(course.getCourseId())
                .writer(MemberResponseDto.of(course.getWriter()))
                .checkWriter(checkWriter)
                .follower(course.getWriter().getFollowInfo().getFollower())
                .following(course.getWriter().getFollowInfo().getFollowing())
                .followStatus(followStatus)
                .title(course.getTitle())
                .thumbnailUrl(course.getThumbnailUrl())
                .scrapCount(course.getScrapCount())
                .description(course.getDescription())
                .startDate(course.getStartDate().toString())
                .address(makeShortenAddress(course.getPlaces().get(0).getGps().getAddress()))
                .duration(course.getDuration())
                .distance(course.getDistance())
                .categories(mergedSet)
                .placeResponseDtos(course.getPlaces().stream()
                        .map(PlaceResponseDto::of)
                        .collect(Collectors.toList()))
                .durationList(durationList)
                .build();
    }
}
