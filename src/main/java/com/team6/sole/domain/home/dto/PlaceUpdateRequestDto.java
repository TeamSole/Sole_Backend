package com.team6.sole.domain.home.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceUpdateRequestDto {
    @ApiModelProperty(value = "고유번호", example = "1")
    private Long placeId;

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

    @ApiModelProperty(value = "장소 이미지들", example = "23.22")
    private List<String> placeImgUrls;
}
