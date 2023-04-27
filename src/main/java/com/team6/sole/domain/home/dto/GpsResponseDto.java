package com.team6.sole.domain.home.dto;

import com.team6.sole.domain.home.HomeService;
import com.team6.sole.domain.home.entity.Gps;
import com.team6.sole.domain.member.entity.Member;
import lombok.*;

import static com.team6.sole.domain.home.HomeService.makeShortenAddress;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GpsResponseDto {
    private Long memberId;

    private String nickname;

    private Gps currentGps;

    @Builder
    public GpsResponseDto(Long memberId, String nickname, Gps currentGps) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.currentGps = currentGps;
    }

    public static GpsResponseDto of(Member member) {
        return GpsResponseDto.builder()
                .memberId(member.getMemberId())
                .nickname(member.getNickname())
                .currentGps(Gps.builder()
                        .address(makeShortenAddress(member.getCurrentGps().getAddress()))
                        .latitude(member.getCurrentGps().getLatitude())
                        .longitude(member.getCurrentGps().getLongitude())
                        .build())
                .build();
    }
}
