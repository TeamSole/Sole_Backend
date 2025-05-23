package com.team6.sole.domain.home.entity;

import com.team6.sole.domain.home.dto.CourseUpdateRequestDto;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.home.model.PlaceCategory;
import com.team6.sole.domain.home.model.Region;
import com.team6.sole.domain.home.model.TransCategory;
import com.team6.sole.domain.home.model.WithCategory;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.global.config.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    private String thumbnailUrl;

    private int scrapCount;

    private String title;

    private String description;

    @Temporal(TemporalType.DATE)
    private Date startDate;

    private int duration;

    private double distance;

    @Enumerated(EnumType.STRING)
    private Region region;

    @ElementCollection(fetch = FetchType.LAZY) @Enumerated(EnumType.STRING)
    Set<PlaceCategory> placeCategories;

    @ElementCollection(fetch = FetchType.LAZY) @Enumerated(EnumType.STRING)
    Set<WithCategory> withCategories;

    @ElementCollection(fetch = FetchType.LAZY) @Enumerated(EnumType.STRING)
    Set<TransCategory> transCategories;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member writer;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Place> places = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CourseMember> courseMembers = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Declaration> declarations = new ArrayList<>();

    public void putPlace(Place place) {
        this.getPlaces().add(place);
    }

    public void addScrapCount() {
        this.scrapCount++;
    }

    public void removeScrapCount() {
        this.scrapCount--;
    }

    public void modCourse(CourseUpdateRequestDto courseUpdateRequestDto, Date startDate, int totalDuration, double totalPlaceDistance, Region region) {
        this.title = courseUpdateRequestDto.getTitle();
        this.startDate = startDate;
        this.duration = totalDuration;
        this.distance = totalPlaceDistance;
        this.region = region;
        this.placeCategories = courseUpdateRequestDto.getPlaceCategories();
        this.withCategories = courseUpdateRequestDto.getWithCategories();
        this.transCategories = courseUpdateRequestDto.getTransCategories();
        this.description = courseUpdateRequestDto.getDescription();
    }

    public void modThumbnailImg(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    @Builder
    public Course(Long courseId, String thumbnailUrl, int scrapCount,
                  String title, String description, Date startDate,
                  int duration, double distance, Region region,
                  Set<PlaceCategory> placeCategories, Set<WithCategory> withCategories, Set<TransCategory> transCategories,
                  Member writer, Member member, List<Place> places, List<CourseMember> courseMembers,
                  List<Declaration> declarations) {
        this.courseId = courseId;
        this.thumbnailUrl = thumbnailUrl;
        this.scrapCount = scrapCount;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.duration = duration;
        this.distance = distance;
        this.region = region;
        this.placeCategories = placeCategories;
        this.withCategories = withCategories;
        this.transCategories = transCategories;
        this.writer = writer;
        this.member = member;
        this.places = places;
        this.courseMembers = courseMembers;
        this.declarations = declarations;
    }
}
