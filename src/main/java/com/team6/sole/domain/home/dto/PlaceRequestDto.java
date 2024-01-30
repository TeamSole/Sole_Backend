package com.team6.sole.domain.home.dto;

import java.util.List;

import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.Place;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceRequestDto {
    @ApiModelProperty(value = "장소이름", example = "짜장면집")
    private String placeName;

    @ApiModelProperty(value = "소요시간", example = "80")
    private int duration;

    @ApiModelProperty(value = "장소설명", example = "중식집")
    private String description;

    @ApiModelProperty(value = "주소", example = "서울시 강남구 논현동 217-41")
    private String address;

    @ApiModelProperty(value = "위도", example = "45.43")
    private double latitude;

    @ApiModelProperty(value = "경도", example = "23.22")
    private double longitude;

    public static Place placeToEntity(List<String> checkplaceImgUrls, PlaceRequestDto placeRequestDto, Course course) {
        return Place.builder()
                .placeName(placeRequestDto.getPlaceName())
                .duration(placeRequestDto.getDuration())
                .description(placeRequestDto.getDescription())
                .gps(GpsReqeustDto.gpsToEntity(placeRequestDto))
                .placeImgUrls(checkplaceImgUrls)
                .course(course)
        .build();
    }
}
