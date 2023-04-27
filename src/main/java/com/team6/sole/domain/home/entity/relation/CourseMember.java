package com.team6.sole.domain.home.entity.relation;

import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.scrap.entity.CourseMemberScrapFolder;
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
public class CourseMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;

    @OneToMany(mappedBy = "courseMember", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CourseMemberScrapFolder> courseMemberScrapFolders = new ArrayList<>();

    @Builder
    public CourseMember(Long courseMemberId, Member member,
                        Course course, List<CourseMemberScrapFolder> courseMemberScrapFolders) {
        this.courseMemberId = courseMemberId;
        this.member = member;
        this.course = course;
        this.courseMemberScrapFolders = courseMemberScrapFolders;
    }
}
