package com.team6.sole.domain.home.repository;

import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseMemberRepository extends JpaRepository<CourseMember, Long> {
    boolean existsByMemberAndCourse_CourseId(Member member, Long courseId);

    List<CourseMember> findByMemberAndScrapFolder_ScrapFolderId(Member member, Long scrapFolderId);

    List<CourseMember> findAllByMemberAndScrapFolder_ScrapFolderIdAndCourse_CourseIdIn(Member member, Long scrapFolderId, List<Long> courseIds);

    void deleteByCourseAndScrapFolder_ScrapFolderIdAndMember(Course course, Long scrapFolderId, Member member);

    Optional<CourseMember> findByCourse_CourseIdAndMember(Long courseId, Member member);

    void deleteByCourse_CourseIdAndMember(Long courseId, Member member);
}
