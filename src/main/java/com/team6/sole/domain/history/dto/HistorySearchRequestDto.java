package com.team6.sole.domain.history.dto;

import com.team6.sole.domain.home.model.PlaceCategory;
import com.team6.sole.domain.home.model.Region;
import com.team6.sole.domain.home.model.TransCategory;
import com.team6.sole.domain.home.model.WithCategory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HistorySearchRequestDto {
    Set<PlaceCategory> placeCategories;

    Set<WithCategory> withCategories;

    Set<TransCategory> transCategories;

    Set<Region> regions;
}
