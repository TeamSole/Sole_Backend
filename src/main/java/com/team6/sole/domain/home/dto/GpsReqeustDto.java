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
    private Gps gps;
}
