package com.team6.sole.domain.home.dto;

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
}
