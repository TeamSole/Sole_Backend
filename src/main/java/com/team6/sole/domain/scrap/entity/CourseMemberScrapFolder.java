package com.team6.sole.domain.scrap.entity;

import com.team6.sole.domain.home.entity.relation.CourseMember;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseMemberScrapFolder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private ScrapFolder scrapFolder;

    @ManyToOne(fetch = FetchType.LAZY)
    private CourseMember courseMember;

    @Builder
    public CourseMemberScrapFolder(Long id, ScrapFolder scrapFolder, CourseMember courseMember) {
        this.id = id;
        this.scrapFolder = scrapFolder;
        this.courseMember = courseMember;
    }
}
