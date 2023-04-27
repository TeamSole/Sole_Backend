package com.team6.sole.domain.home.entity;

import com.team6.sole.global.config.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long placeId;

    private String placeName;

    private int duration;

    private String description;

    @Embedded
    private Gps gps;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<String> placeImgUrls = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;

    public void modPlace(String placeName, int duration, String description, Gps gps) {
        this.placeName = placeName;
        this.duration = duration;
        this.description = description;
        this.gps = gps;
    }

    public void modPlaceImgUrls(List<String> placeImgUrls) {
        this.placeImgUrls = placeImgUrls;
    }

    @Builder
    public Place(Long placeId, String placeName, int duration, String description,
                 Gps gps, List<String> placeImgUrls, Course course) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.description = description;
        this.gps = gps;
        this.placeImgUrls = placeImgUrls;
        this.course = course;
    }
}
