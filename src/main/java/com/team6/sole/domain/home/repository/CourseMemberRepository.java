package com.team6.sole.domain.home.repository;

import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseMemberRepository extends JpaRepository<CourseMember, Long> {
    boolean existsByMemberAndCourse_CourseId(Member member, Long courseId);

    void deleteByMemberAndCourse(Member member, Course course);
}
