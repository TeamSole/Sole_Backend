package com.team6.sole.global.config.security.oauth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.team6.sole.domain.home.dto.GeocodeResponseDto;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.BadRequestException;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.*;

@Component
@RequiredArgsConstructor
public class GoogleUtils {
    private final WebClient webClient;

    @Value("${GOOGLE.APP_KEY}")
    private String APP_KEY;

    // 현재 위치 좌표 -> 주소 변환
    @SneakyThrows
    public String convertAdress(Double latitude, Double longitude) {
        String getAdressURL = "https://maps.googleapis.com/maps/api/geocode/json";

        try {
            return Objects.requireNonNull(webClient
                            .get()
                            .uri(getAdressURL, builder -> builder
                                    .queryParam("latlng", latitude + "," + longitude)
                                    .queryParam("language", "ko")
                                    .queryParam("key", APP_KEY)
                                    .build())
                            .retrieve()
                            .bodyToMono(GeocodeResponseDto.class)
                            .block()).getResults()[0].getFormatted_address();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(ErrorCode.GOOGLE_BAD_REQUEST);
        }
    }
}
