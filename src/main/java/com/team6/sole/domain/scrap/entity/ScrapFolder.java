package com.team6.sole.domain.scrap.entity;

import com.team6.sole.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScrapFolder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scrapFolderId;

    private String scrapFolderName;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @OneToMany(mappedBy = "scrapFolder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CourseMemberScrapFolder> courseMemberScrapFolders = new ArrayList<>();

    public void modScrapFolderName(String scrapFolderName) {
        this.scrapFolderName = scrapFolderName;
    }

    @Builder
    public ScrapFolder(Long scrapFolderId, String scrapFolderName,
                       Member member, List<CourseMemberScrapFolder> courseMemberScrapFolders) {
        this.scrapFolderId = scrapFolderId;
        this.scrapFolderName = scrapFolderName;
        this.member = member;
        this.courseMemberScrapFolders = courseMemberScrapFolders;
    }
}
