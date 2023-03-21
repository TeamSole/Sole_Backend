package com.team6.sole.domain.home.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeocodeResponseDto {
    private String status;
    private GeocodeResponseDto.Results[] results;

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class Results {
        private String formatted_address;
        private GeocodeResponseDto.Results.Geometry geometry;

        @Getter
        @NoArgsConstructor(access = AccessLevel.PROTECTED)
        public static class Geometry {
            private GeocodeResponseDto.Results.Geometry.Location location;

            @Getter
            @NoArgsConstructor(access = AccessLevel.PROTECTED)
            public static class Location {
                private String lat;
                private String lng;
            }
        }
    }
}
