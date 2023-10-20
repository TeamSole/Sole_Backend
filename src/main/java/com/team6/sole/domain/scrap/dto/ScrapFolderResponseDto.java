package com.team6.sole.domain.scrap.dto;

import com.team6.sole.domain.scrap.entity.ScrapFolder;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScrapFolderResponseDto {
    private Long scrapFolderId;

    private String scrapFolderName;

    private String scrapFolderImg;

    private int scrapCount;

    @Builder
    public ScrapFolderResponseDto(Long scrapFolderId, String scrapFolderName,
                                  String scrapFolderImg, int scrapCount) {
        this.scrapFolderId = scrapFolderId;
        this.scrapFolderName = scrapFolderName;
        this.scrapFolderImg = scrapFolderImg;
        this.scrapCount = scrapCount;
    }

    public static ScrapFolderResponseDto of(ScrapFolder scrapFolder) {
        return ScrapFolderResponseDto.builder()
                .scrapFolderId(scrapFolder.getScrapFolderId())
                .scrapFolderName(scrapFolder.getScrapFolderName())
                .scrapFolderImg(scrapFolder.getCourseMembers().isEmpty()
                                ? null
                                : scrapFolder.getCourseMembers().get(0).getCourse().getThumbnailUrl())
                .scrapCount(scrapFolder.getCourseMembers().isEmpty()
                                ? 0
                                : scrapFolder.getCourseMembers().size())
                .build();
    }
}
