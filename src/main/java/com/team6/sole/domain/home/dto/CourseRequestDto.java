package com.team6.sole.domain.home.dto;

import com.team6.sole.domain.home.entity.Category;
import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.Declaration;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.home.model.Region;
import com.team6.sole.domain.home.model.PlaceCategory;
import com.team6.sole.domain.home.model.TransCategory;
import com.team6.sole.domain.home.model.WithCategory;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.scrap.entity.ScrapFolder;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseRequestDto {
    @ApiModelProperty(value = "코스제목", example = "제주도 여행...!")
    private String title;

    @ApiModelProperty(value = "코스설명", example = "제주도로 떠나요~~")
    private String description;

    @ApiModelProperty(value = "간 날짜", example = "2023-03-02")
    private String date;

    @ApiModelProperty(value = "장소 카테고리")
    private Set<PlaceCategory> placeCategories;

    @ApiModelProperty(value = "함께하는 사람 카테로기")
    private Set<WithCategory> withCategories;

    @ApiModelProperty(value = "대중교통 카테고리")
    private Set<TransCategory> transCategories;

    private List<PlaceRequestDto> placeRequestDtos;

    public static Course courseToEntity(String thumbnailUrl, double totalDistance,
                                        Date startDate, Region region, int duration, 
                                        Member writer,
                                        CourseRequestDto courseRequestDto) {
        return Course.builder()
                .thumbnailUrl(thumbnailUrl)
                .scrapCount(0)
                .title(courseRequestDto.getTitle())
                .description(courseRequestDto.getDescription())
                .startDate(startDate)
                .duration(duration)
                .distance(totalDistance)
                .region(region)
                .placeCategories(courseRequestDto.getPlaceCategories())
                .withCategories(courseRequestDto.getWithCategories())
                .transCategories(courseRequestDto.getTransCategories())
                .writer(writer)
                .places(new ArrayList<>())
        .build();
    }

    public static CourseMember courseMemberToEntity(Course course, Member member, ScrapFolder scrapFolder) {
        return CourseMember.builder()
                .course(course)
                .member(member)
                .scrapFolder(scrapFolder)
        .build();
    }

    public static Declaration declarationToEntity(Course course, Member member) {
        return Declaration.builder()
                .course(course)
                .member(member)
        .build();
    }

    public static Category categoriesToEntity(FavCategoryRequestDto favCategoryRequestDto) {
        return Category.builder()
                .placeCategories(favCategoryRequestDto.getPlaceCategories())
                .withCategories(favCategoryRequestDto.getWithCategories())
                .transCategories(favCategoryRequestDto.getTransCategories())
        .build();
    }
}
