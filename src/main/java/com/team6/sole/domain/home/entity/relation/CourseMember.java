package com.team6.sole.domain.home.entity.relation;

import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.scrap.entity.ScrapFolder;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    private ScrapFolder scrapFolder;

    public void modScrapFolder(ScrapFolder scrapFolder) {
        this.scrapFolder = scrapFolder;
    }

    @Builder
    public CourseMember(Long courseMemberId, Member member,
                        Course course, ScrapFolder scrapFolder) {
        this.courseMemberId = courseMemberId;
        this.member = member;
        this.course = course;
        this.scrapFolder = scrapFolder;
    }
}
