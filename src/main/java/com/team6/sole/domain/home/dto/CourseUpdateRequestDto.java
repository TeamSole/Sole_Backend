package com.team6.sole.domain.home.dto;

import com.team6.sole.domain.home.model.PlaceCategory;
import com.team6.sole.domain.home.model.TransCategory;
import com.team6.sole.domain.home.model.WithCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseUpdateRequestDto {
    private String title;

    private String startDate;

    private Set<PlaceCategory> placeCategories;

    private Set<WithCategory> withCategories;

    private Set<TransCategory> transCategories;

    private String description;

    private List<PlaceUpdateRequestDto> placeUpdateRequestDtos;
}
