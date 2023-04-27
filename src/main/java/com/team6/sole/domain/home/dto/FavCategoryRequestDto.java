package com.team6.sole.domain.home.dto;

import com.team6.sole.domain.home.model.PlaceCategory;
import com.team6.sole.domain.home.model.TransCategory;
import com.team6.sole.domain.home.model.WithCategory;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavCategoryRequestDto {
    @ApiModelProperty(value = "장소 카테고리")
    private Set<PlaceCategory> placeCategories;

    @ApiModelProperty(value = "함께하는 사람 카테로기")
    private Set<WithCategory> withCategories;

    @ApiModelProperty(value = "대중교통 카테고리")
    private Set<TransCategory> transCategories;
}
