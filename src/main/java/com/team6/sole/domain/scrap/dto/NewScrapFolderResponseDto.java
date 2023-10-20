package com.team6.sole.domain.scrap.dto;

import com.team6.sole.domain.home.entity.relation.CourseMember;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NewScrapFolderResponseDto {
    private String scrapFolderName;

    private List<String> courseTitles;

    @Builder
    public NewScrapFolderResponseDto(Long id, String scrapFolderName, List<String> courseTitles) {
        this.scrapFolderName = scrapFolderName;
        this.courseTitles = courseTitles;
    }

    public static NewScrapFolderResponseDto of(String scrapFolderName, List<CourseMember> courseMembers) {
        return NewScrapFolderResponseDto.builder()
                .scrapFolderName(scrapFolderName)
                .courseTitles(courseMembers.stream()
                        .map(courseMember -> courseMember.getCourse().getTitle())
                        .collect(Collectors.toList()))
                .build();
    }
}
