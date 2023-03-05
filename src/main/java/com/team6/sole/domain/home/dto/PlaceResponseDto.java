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

    private String placeName;

    private String description;

    private String address;

    private List<String> placeImgUrls = new ArrayList<>();

    @Builder
    public PlaceResponseDto(Long placeId, String placeName, String description,
                            String address, List<String> placeImgUrls) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.description = description;
        this.address = address;
        this.placeImgUrls = placeImgUrls;
    }

    public static PlaceResponseDto of(Place place) {
        return PlaceResponseDto.builder()
                .placeId(place.getPlaceId())
                .placeName(place.getPlaceName())
                .description(place.getDescription())
                .address(place.getGps().getAddress())
                .placeImgUrls(place.getPlaceImgUrls())
                .build();
    }
}
