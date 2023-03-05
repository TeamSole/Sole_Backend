package com.team6.sole.domain.home.entity.relation;

import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.member.entity.Member;
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

    @Builder
    public CourseMember(Long courseMemberId, Member member, Course course) {
        this.courseMemberId = courseMemberId;
        this.member = member;
        this.course = course;
    }
}
