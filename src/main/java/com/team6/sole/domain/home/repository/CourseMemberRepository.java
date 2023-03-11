package com.team6.sole.domain.home.repository;

import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseMemberRepository extends JpaRepository<CourseMember, Long> {
    boolean existsByMemberAndCourse_CourseId(Member member, Long courseId);

    void deleteByMemberAndCourse(Member member, Course course);

    List<CourseMember> findByMember_SocialId(String socialId);

    List<CourseMember> findAllByCourse_CourseIdIn(List<Long> courseIds);

    void deleteByCourse_CourseIdAndMember_SocialId(Long courseId, String socialId);

    CourseMember findByCourse_CourseIdAndMember_SocialId(Long courseId, String socialId);
}
