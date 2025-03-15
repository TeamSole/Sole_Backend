package com.team6.sole.domain.scrap.dto;

import java.util.ArrayList;

import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.scrap.entity.ScrapFolder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScrapFolderRequestDto {
    private String scrapFolderName;

    public static ScrapFolder scrapFolderToEntity(Member member, ScrapFolderRequestDto scrapFolderRequestDto) {
        return ScrapFolder.builder()
            .scrapFolderName(scrapFolderRequestDto.getScrapFolderName())
            .courseMembers(new ArrayList<>())
            .member(member)
            .build();
    }
}
