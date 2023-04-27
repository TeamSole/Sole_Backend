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
                .scrapFolderImg(scrapFolder.getCourseMemberScrapFolders().isEmpty()
                                ? null
                                : scrapFolder.getCourseMemberScrapFolders().get(0).getCourseMember().getCourse().getThumbnailUrl())
                .scrapCount(scrapFolder.getCourseMemberScrapFolders().isEmpty()
                                ? 0
                                : scrapFolder.getCourseMemberScrapFolders().size())
                .build();
    }
}
