package com.team6.sole.domain.home.entity;

import com.team6.sole.domain.home.entity.relation.CourseMember;
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

    private int distance;

    @Embedded
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member writer;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Place> places = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CourseMember> courseMembers = new ArrayList<>();

    public void putPlace(Place place) {
        this.getPlaces().add(place);
    }

    @Builder
    public Course(Long courseId, String thumbnailUrl, int scrapCount,
                  String title, String description, Date startDate,
                  int duration, int distance, Category category, Member writer,
                  List<Place> places, List<CourseMember> courseMembers) {
        this.courseId = courseId;
        this.thumbnailUrl = thumbnailUrl;
        this.scrapCount = scrapCount;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.duration = duration;
        this.distance = distance;
        this.category = category;
        this.writer = writer;
        this.places = places;
        this.courseMembers = courseMembers;
    }
}
