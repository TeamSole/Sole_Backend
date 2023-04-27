package com.team6.sole.domain.home.repository;

import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CourseMemberRepository extends JpaRepository<CourseMember, Long> {
    boolean existsByMemberAndCourse_CourseId(Member member, Long courseId);

    void deleteByMember_SocialIdAndCourse_CourseId(String socialId, Long courseId);

    List<CourseMember> findByMember_SocialId(String socialId);

    List<CourseMember> findAllByMember_SocialIdAndCourse_CourseIdIn(String socialId, List<Long> courseIds);

    void deleteByCourse_CourseIdAndMember_SocialId(Long courseId, String socialId);

    CourseMember findByCourse_CourseIdAndMember_SocialId(Long courseId, String socialId);

    Optional<CourseMember> findByMember_SocialIdAndCourse_CourseId(String socialId, Long courseId);
}
