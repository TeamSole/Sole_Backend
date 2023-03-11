package com.team6.sole.domain.scrap.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewScrapFolderRequestDto {
    private List<Long> courseIds;
}
