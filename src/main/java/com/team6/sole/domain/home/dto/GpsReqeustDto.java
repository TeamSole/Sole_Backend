package com.team6.sole.domain.home.dto;

import com.team6.sole.domain.home.entity.Gps;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GpsReqeustDto {
    private Double latitude;
    private Double longitude;

    public static Gps gpsToEntity(String address, GpsReqeustDto gpsReqeustDto) {
        return Gps.builder()
            .address(address)
            .latitude(gpsReqeustDto.getLatitude())
            .longitude(gpsReqeustDto.getLongitude())
        .build();
    }

    public static Gps gpsToEntity(PlaceRequestDto placeRequestDto) {
        return Gps.builder()
            .address(placeRequestDto.getAddress())
            .latitude(placeRequestDto.getLatitude())
            .longitude(placeRequestDto.getLongitude())
        .build();
    }
}
