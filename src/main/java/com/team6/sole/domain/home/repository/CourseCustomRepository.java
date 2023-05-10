package com.team6.sole.domain.home.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team6.sole.domain.history.dto.HistorySearchRequestDto;
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
                        (QCourse.course.title.contains(title)))
                .orderBy(QCourse.course.courseId.desc())
                .limit(5)
                .fetch();
    }

    public List<Course> findAllByWriter(Long courseId, Member writer) {
        return jpaQueryFactory
                .selectFrom(QCourse.course)
                .where(ltCourseId(courseId), // 동적 쿼리
                        (QCourse.course.writer.eq(writer)))
                .orderBy(QCourse.course.courseId.desc())
                .limit(5)
                .fetch();
    }

    // 나의 기록 보기(하단)(5개 + 5n)
    public List<Course> findAllByCatgegoryAndWriter(Long courseId,
                                                    Member writer,
                                                    HistorySearchRequestDto historySearchRequestDto) {

        return jpaQueryFactory
                .selectFrom(QCourse.course)
                .where(ltCourseId(courseId), // 동적 쿼리
                        filterCategories(historySearchRequestDto),
                        filterRegions(historySearchRequestDto),
                        filterWriter(writer))
                .orderBy(QCourse.course.courseId.desc())
                .limit(5)
                .fetch();
    }

    private BooleanExpression ltCourseId(Long courseId) {
        return courseId != null // BooleanExpression 자리에 null이 반환되면 조건문에서 자동으로 제거된다
                ? QCourse.course.courseId.lt(courseId)
                : null;
    }

    private BooleanExpression filterRegions(HistorySearchRequestDto historySearchRequestDto) {
        return historySearchRequestDto.getRegions() != null
                ? QCourse.course.region.in((historySearchRequestDto.getRegions()))
                : null;
    }

    private BooleanExpression filterCategories(HistorySearchRequestDto historySearchRequestDto) {
        if (historySearchRequestDto.getPlaceCategories() == null
                && historySearchRequestDto.getWithCategories() == null
                && historySearchRequestDto.getTransCategories() == null) {
            return null;
        }

        return QCourse.course.placeCategories.any().in(historySearchRequestDto.getPlaceCategories())
                .or(QCourse.course.withCategories.any().in(historySearchRequestDto.getWithCategories())
                        .or(QCourse.course.transCategories.any().in(historySearchRequestDto.getTransCategories())));
    }

    private BooleanExpression filterWriter(Member writer) {
        return QCourse.course.writer.eq(writer);
    }
}