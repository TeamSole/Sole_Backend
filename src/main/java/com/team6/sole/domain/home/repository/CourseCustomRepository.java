package com.team6.sole.domain.home.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.QCourse;
import com.team6.sole.domain.home.model.PlaceCategory;
import com.team6.sole.domain.home.model.TransCategory;
import com.team6.sole.domain.home.model.WithCategory;
import com.team6.sole.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class CourseCustomRepository {
    private final JPAQueryFactory jpaQueryFactory;

    // 홈 보기
    public List<Course> findAllByCategory(Long courseId,
                                        Set<PlaceCategory> placeCategories,
                                        Set<WithCategory> withCategories,
                                        Set<TransCategory> transCategories) {

        return jpaQueryFactory
                .selectFrom(QCourse.course)
                .where(ltCourseId(courseId), // 동적 쿼리
                        (QCourse.course.placeCategories.any().in(placeCategories))
                        .or(QCourse.course.withCategories.any().in(withCategories))
                        .or(QCourse.course.transCategories.any().in(transCategories)))
                .orderBy(QCourse.course.courseId.desc())
                .limit(5)
                .fetch();
    }

    // 홈 검색
    public List<Course> findAllByTitleContaining(Long courseId, String title) {
        return jpaQueryFactory
                .selectFrom(QCourse.course)
                .where(ltCourseId(courseId), // 동적 쿼리
                        (QCourse.course.title.like("%" + title + "%")))
                .orderBy(QCourse.course.courseId.desc())
                .limit(5)
                .fetch();
    }

    public List<Course> findAllByWriter(Long courseId, Member writer) {
        return jpaQueryFactory
                .selectFrom(QCourse.course)
                .where(ltCourseId(courseId), // 동적 쿼리 // 동적 쿼리
                        (QCourse.course.writer.eq(writer)))
                .orderBy(QCourse.course.createdAt.desc())
                .limit(5)
                .fetch();
    }

    // 홈 보기
    public List<Course> findAllByCatgegoryAndWriter(Long courseId,
                                           Member writer,
                                           Set<PlaceCategory> placeCategories,
                                           Set<WithCategory> withCategories,
                                           Set<TransCategory> transCategories) {
        return jpaQueryFactory
                .selectFrom(QCourse.course)
                .where(ltCourseId(courseId), // 동적 쿼리 // 동적 쿼리
                        (QCourse.course.placeCategories.any().in(placeCategories))
                        .or(QCourse.course.withCategories.any().in(withCategories))
                        .or(QCourse.course.transCategories.any().in(transCategories))
                        .and(QCourse.course.writer.eq(writer)))
                .orderBy(QCourse.course.courseId.desc())
                .limit(5)
                .fetch();
    }

    private BooleanExpression ltCourseId(Long courseId) {
        if (courseId == null) {
            return null; // BooleanExpression 자리에 null이 반환되면 조건문에서 자동으로 제거된다
        }

        return QCourse.course.courseId.lt(courseId);
    }
}