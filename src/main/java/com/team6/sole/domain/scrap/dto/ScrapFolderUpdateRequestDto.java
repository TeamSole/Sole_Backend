package com.team6.sole.domain.scrap.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScrapFolderUpdateRequestDto {
    private String scrapFolderName;
}
