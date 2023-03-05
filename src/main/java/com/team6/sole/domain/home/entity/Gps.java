package com.team6.sole.domain.home.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gps {
    // 주소
    private String address;

    // 위도
    private double latitude;

    // 경도
    private double longitude;

    @Builder
    public Gps(String address, double latitude, double longitude) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
