package com.team6.sole.domain.home;

import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.repository.CourseRepository;
import com.team6.sole.domain.member.MemberRepository;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.infra.direction.DirectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecommendCourseScheduler {
    private final MemberRepository memberRepository;
    private final DirectionService directionService;

    @Async("home")
    @Transactional
    @CacheEvict(value = "recommends", allEntries = true) // 캐싱 초기화
    @Scheduled(cron = "0 0 0/1 * * *") // 매 1시간마다(01:00 02:00 03:00 ....)
    public void recommendCourses() {
        List<Member> members = memberRepository.findAll();

        for (Member member : members) {
            List<Course> recommendCourses = directionService.buildCourses(member.getCurrentGps());
            member.setRecommendCourses(recommendCourses);

            memberRepository.saveAndFlush(member);
        }
    }
}
