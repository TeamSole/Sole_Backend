package com.team6.sole.domain.history.dto;

import com.team6.sole.domain.home.model.PlaceCategory;
import com.team6.sole.domain.home.model.TransCategory;
import com.team6.sole.domain.member.entity.Member;
import lombok.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistoryResponseDto {
    private String nickname;

    private int totalDate;

    private int totalPlaces;

    private int totalCourses;

    private String mostRegion;

    private Set<TransCategory> mostTransCategories;

    private Set<PlaceCategory> mostPlaceCategories;

    @Builder
    public HistoryResponseDto(String nickname, int totalDate, int totalPlaces, int totalCourses,
                              String mostRegion,
                              Set<TransCategory> mostTransCategories, Set<PlaceCategory> mostPlaceCategories) {
        this.nickname = nickname;
        this.totalDate = totalDate;
        this.totalPlaces = totalPlaces;
        this.totalCourses = totalCourses;
        this.mostRegion = mostRegion;
        this.mostTransCategories = mostTransCategories;
        this.mostPlaceCategories = mostPlaceCategories;
    }

    public static HistoryResponseDto of(Member writer,
                                        Set<PlaceCategory> mostPlaceCategories,
                                        Set<TransCategory> mostTransCategories) {
        LocalDate startDate = LocalDate.from(writer.getCreatedAt());
        LocalDate endDate = LocalDate.now();
        LocalDateTime date1 = startDate.atStartOfDay();
        LocalDateTime date2 = endDate.atStartOfDay();
        int betweenDays = (int) Duration.between(date1, date2).toDays();

        return HistoryResponseDto.builder()
                .nickname(writer.getNickname())
                .totalDate(betweenDays)
                .totalPlaces(writer.getCourses().stream()
                        .mapToInt(i -> i.getPlaces().size())
                        .sum())
                .totalCourses(writer.getCourses().size())
                .mostRegion(null)
                .mostTransCategories(mostTransCategories)
                .mostPlaceCategories(mostPlaceCategories)
                .build();
    }
}
