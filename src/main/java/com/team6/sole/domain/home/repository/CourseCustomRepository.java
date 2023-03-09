package com.team6.sole.domain.home.repository;

import com.querydsl.core.BooleanBuilder;
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
    public List<Course> findAllByCatgegory(Long courseId,
                                        Set<PlaceCategory> placeCategories,
                                        Set<WithCategory> withCategories,
                                        Set<TransCategory> transCategories) {

        // 1. id < 파라미터를 첫 페이지에선 사용하지 않기 위한 동적 쿼리
        BooleanBuilder dynamicLtId = new BooleanBuilder();

        if (courseId != null) {
            dynamicLtId.and(QCourse.course.courseId.lt(courseId));
        }

        return jpaQueryFactory
                .selectFrom(QCourse.course)
                .where(dynamicLtId // 동적 쿼리
                        .or(QCourse.course.placeCategories.any().in(placeCategories))
                        .or(QCourse.course.withCategories.any().in(withCategories))
                        .or(QCourse.course.transCategories.any().in(transCategories)))
                .orderBy(QCourse.course.courseId.desc())
                .limit(5)
                .fetch();
    }

    public List<Course> findAllByTitleContaining(Long courseId, String title) {
        // 1. id < 파라미터를 첫 페이지에선 사용하지 않기 위한 동적 쿼리
        BooleanBuilder dynamicLtId = new BooleanBuilder();

        if (courseId != null) {
            dynamicLtId.and(QCourse.course.courseId.lt(courseId));
        }

        return jpaQueryFactory
                .selectFrom(QCourse.course)
                .where(dynamicLtId // 동적 쿼리
                        .and(QCourse.course.title.like("%" + title + "%")))
                .orderBy(QCourse.course.courseId.desc())
                .limit(10)
                .fetch();
    }

    public List<Course> findAllByWriter(Long courseId, Member writer) {
        // 1. id < 파라미터를 첫 페이지에선 사용하지 않기 위한 동적 쿼리
        BooleanBuilder dynamicLtId = new BooleanBuilder();

        if (courseId != null) {
            dynamicLtId.and(QCourse.course.courseId.lt(courseId));
        }

        return jpaQueryFactory
                .selectFrom(QCourse.course)
                .where(dynamicLtId // 동적 쿼리
                        .and(QCourse.course.writer.eq(writer)))
                .orderBy(QCourse.course.createdAt.desc())
                .limit(10)
                .fetch();
    }
}