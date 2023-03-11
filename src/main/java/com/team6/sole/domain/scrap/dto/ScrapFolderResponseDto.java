package com.team6.sole.domain.scrap.dto;

import com.team6.sole.domain.scrap.entity.ScrapFolder;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScrapFolderResponseDto {
    private Long scrapFolderId;

    private String scrapFolderName;

    @Builder
    public ScrapFolderResponseDto(Long scrapFolderId, String scrapFolderName) {
        this.scrapFolderId = scrapFolderId;
        this.scrapFolderName = scrapFolderName;
    }

    public static ScrapFolderResponseDto of(ScrapFolder scrapFolder) {
        return ScrapFolderResponseDto.builder()
                .scrapFolderId(scrapFolder.getScrapFolderId())
                .scrapFolderName(scrapFolder.getScrapFolderName())
                .build();
    }
}
