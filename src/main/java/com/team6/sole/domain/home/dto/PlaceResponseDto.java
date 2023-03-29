package com.team6.sole.domain.home.dto;

import com.team6.sole.domain.home.entity.Place;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceResponseDto {
    private Long placeId;

    private int duration;

    private String placeName;

    private String description;

    private String address;

    private double latitude;

    private double longitude;

    private List<String> placeImgUrls = new ArrayList<>();

    @Builder
    public PlaceResponseDto(Long placeId, int duration, String placeName, String description,
                            String address, List<String> placeImgUrls,
                            double latitude, double longitude) {
        this.placeId = placeId;
        this.duration = duration;
        this.placeName = placeName;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.placeImgUrls = placeImgUrls;
    }

    public static PlaceResponseDto of(Place place) {
        return PlaceResponseDto.builder()
                .placeId(place.getPlaceId())
                .duration(place.getDuration())
                .placeName(place.getPlaceName())
                .description(place.getDescription())
                .address(place.getGps().getAddress())
                .latitude(place.getGps().getLatitude())
                .longitude(place.getGps().getLongitude())
                .placeImgUrls(place.getPlaceImgUrls())
                .build();
    }
}
