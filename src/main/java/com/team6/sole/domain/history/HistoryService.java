package com.team6.sole.domain.history;

import com.team6.sole.domain.history.dto.HistoryResponseDto;
import com.team6.sole.domain.history.dto.HistorySearchRequestDto;
import com.team6.sole.domain.home.dto.HomeResponseDto;
import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.Gps;
import com.team6.sole.domain.home.entity.Place;
import com.team6.sole.domain.home.model.PlaceCategory;
import com.team6.sole.domain.home.model.TransCategory;
import com.team6.sole.domain.home.repository.CourseCustomRepository;
import com.team6.sole.domain.member.MemberRepository;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final MemberRepository memberRepository;
    private final CourseCustomRepository courseCustomRepository;

    // 나의 기록 보기(상단)
    @Transactional(readOnly = true)
    public HistoryResponseDto showMyHistory(String socialId) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        List<PlaceCategory> placeCategories = new ArrayList<>();
        List<TransCategory> transCategories = new ArrayList<>();
        for (Course course : member.getCourses()) {
            placeCategories.addAll(course.getPlaceCategories());
            transCategories.addAll(course.getTransCategories());
        }

        return HistoryResponseDto.of(
                member,
                makeMostRegion(member.getCourses()),
                makeMostPlaceCategories(placeCategories),
                makeMostTransCategories(transCategories));
    }

    // 나의 기록 보기(하단)(5개 + 5n)
    @Transactional(readOnly = true)
    public List<HomeResponseDto> showMyCourseHistories(String socialId, Long courseId, HistorySearchRequestDto historySearchRequestDto) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        // 내가 기록한 코스 findAll
        List<Course> courses = courseCustomRepository.findAllByWriter(courseId, member);

        // 내가 기록한 코스 중 검색 조건에 맞는 코스만 필터링
        if (historySearchRequestDto != null) {
            List<Course> filterCourses = courseCustomRepository.findAllByCatgegoryAndWriter(
                    courseId,
                    member,
                    historySearchRequestDto.getPlaceCategories(),
                    historySearchRequestDto.getWithCategories(),
                    historySearchRequestDto.getTransCategories());

            return filterCourses.stream()
                    .map(course -> HomeResponseDto.of(course, true))
                    .collect(Collectors.toList());
        }

        return courses.stream()
                .map(course -> HomeResponseDto.of(course, true))
                .collect(Collectors.toList());
    }

    // 장소 최빈값
    public String makeMostRegion(List<Course> courses) {
        List<String> regions = courses.stream()
                .map(course -> course.getPlaces().get(0))
                .map(Place::getGps)
                .map(Gps::getAddress)
                .map(address -> address.substring(0, address.indexOf("구") + 1))
                .collect(Collectors.toList());

        HashMap<String, Integer> dic = new HashMap<>();
        for (String region : regions) {
            int x = 1;
            //①
            if (dic.containsKey(region)) {
                //③
                x = dic.get(region) + 1;
            }
            //②
            dic.put(region, x);
        }

        return dic.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .limit(1)
                .collect(Collectors.toList()).get(0);
    }

    // 장소 카테고리 mapToSet
    public Set<PlaceCategory> makeMostPlaceCategories(List<PlaceCategory> placeCategories) {
        HashMap<PlaceCategory, Integer> dic = new HashMap<>();
        for (PlaceCategory placeCategory : placeCategories) {
            int x = 1;
            //①
            if (dic.containsKey(placeCategory)) {
                //③
                x = dic.get(placeCategory) + 1;
            }
            //②
            dic.put(placeCategory, x);
        }

        //④
        return dic.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .limit(1)
                .collect(Collectors.toSet());
    }

    // 대중교통 카테고리 mapToSet
    public Set<TransCategory> makeMostTransCategories(List<TransCategory> transCategories) {
        HashMap<TransCategory, Integer> dic = new HashMap<>();
        for (TransCategory transCategory : transCategories) {
            int x = 1;
            //①
            if (dic.containsKey(transCategory)) {
                //③
                x = dic.get(transCategory) + 1;
            }
            //②
            dic.put(transCategory, x);
        }

        //④
        return dic.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .limit(1)
                .collect(Collectors.toSet());
    }
}
