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
import com.team6.sole.domain.home.repository.CourseMemberRepository;
import com.team6.sole.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final CourseCustomRepository courseCustomRepository;
    private final CourseMemberRepository courseMemberRepository;

    // 나의 기록 보기(상단)
    @Transactional(readOnly = true)
    public HistoryResponseDto showMyHistory(Member member) {
        List<PlaceCategory> placeCategories = new ArrayList<>();
        List<TransCategory> transCategories = new ArrayList<>();
        setCategories(member, placeCategories, transCategories);

        return HistoryResponseDto.of(
                member,
                makeMostRegion(member.getCourses()),
                makeMostPlaceCategories(placeCategories),
                makeMostTransCategories(transCategories));
    }

    // 멤버 선호카테고리 전체보기
    public void setCategories(Member member, List<PlaceCategory> placeCategories, List<TransCategory> transCategories) {
        for (Course course : member.getCourses()) {
            placeCategories.addAll(course.getPlaceCategories());
            transCategories.addAll(course.getTransCategories());
        }
    }

    // 장소 최빈값
    public String makeMostRegion(List<Course> courses) {
        if (courses.isEmpty()) {
            return null;
        }

        List<String> regions = getRegionsByCourses(courses);

        HashMap<String, Integer> dic = makeAllRegions(regions);

        return makeMostRegion(dic);
    }

    public List<String> getRegionsByCourses(List<Course> courses) {
        return courses.stream()
            .map(course -> course.getPlaces().get(0))
            .map(Place::getGps)
            .map(Gps::getAddress)
            .map(address -> address.substring(0, address.indexOf("구") + 1))
        .collect(Collectors.toList());
    }

    public HashMap<String, Integer> makeAllRegions(List<String> regions) {
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

        return dic;
    }

    public String makeMostRegion(HashMap<String, Integer> dic) {
        return dic.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .map(Map.Entry::getKey)
            .limit(1)
            .collect(Collectors.toList()).get(0);
    }

    // 장소 카테고리 최빈값(mapToSet)
    public Set<PlaceCategory> makeMostPlaceCategories(List<PlaceCategory> placeCategories) {
        if (placeCategories.isEmpty()) {
            return Collections.emptySet();
        }

        HashMap<PlaceCategory, Integer> dic = makeAllPlaceCategories(placeCategories);

        return makeMostPlaceCategory(dic);
    }

    public HashMap<PlaceCategory, Integer> makeAllPlaceCategories(List<PlaceCategory> placeCategories) {
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

        return dic;
    }

    public Set<PlaceCategory> makeMostPlaceCategory(HashMap<PlaceCategory, Integer> dic) {
        return dic.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .limit(1)
                .collect(Collectors.toSet());
    }

    // 대중교통 카테고리 최빈값(mapSet)
    public Set<TransCategory> makeMostTransCategories(List<TransCategory> transCategories) {
        if (transCategories.isEmpty()) {
            return Collections.emptySet();
        }

        HashMap<TransCategory, Integer> dic = makeAllTransCategories(transCategories);

        return makeMostTransCategory(dic);
    }

    public HashMap<TransCategory, Integer> makeAllTransCategories(List<TransCategory> transCategories) {
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

        return dic;
    }

    public Set<TransCategory> makeMostTransCategory(HashMap<TransCategory, Integer> dic) {
        return dic.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .limit(1)
                .collect(Collectors.toSet());
    }

    // 나의 기록 보기(하단)(5개 + 5n)
    @Transactional(readOnly = true)
    public List<HomeResponseDto> showMyCourseHistories(Member member, Long courseId, HistorySearchRequestDto historySearchRequestDto) {
        if (historySearchRequestDto == null) {
            // 내가 기록한 코스 findAll
            List<Course> courses = courseCustomRepository.findAllByWriter(courseId, member);
            return showRecentCourses(courses, member);
        }

        // 내가 기록한 코스 중 검색 조건에 맞는 코스만 필터링
        List<Course> filterCourses = makeFilterCourses(member, courseId, historySearchRequestDto);

        return showRecentFilterCourses(filterCourses, member, historySearchRequestDto);
    }

    // 최근 코스 모아보기
    public List<HomeResponseDto> showRecentCourses(List<Course> courses, Member member) {
        return courses.stream()
                .map(recent -> HomeResponseDto.of(
                        recent, 
                        isCourseScrap(member, recent.getCourseId()), 
                        isFinalPage(courses, member)))
                .collect(Collectors.toList());
    }

    // 팔로잉한 사람들의 코스들 중 스크랩한 코스(T/F)
    public Boolean isCourseScrap(Member member, Long courseId) {
        return courseMemberRepository.existsByMemberAndCourse_CourseId(member, courseId);
    }

    // 마지막 페이지 판별(TODO -> PageDTO 클래스 따로파서 static method로)
    public Boolean isFinalPage(List<Course> courses, Member member) {
        return courses.size() - 1 != -1 
                && courseCustomRepository.findAllByWriter(
                        courses.get(courses.size() - 1).getCourseId(),
                        member).isEmpty();
    }

    // 최근 코스 모아보기(검색 필터 추가)
    public List<HomeResponseDto> showRecentFilterCourses(List<Course> filterCourses, Member member, HistorySearchRequestDto historySearchRequestDto) {
        return filterCourses.stream()
                .map(course -> HomeResponseDto.of(
                        course,
                        isCourseScrap(member, course.getCourseId()),
                        isSearchFinalPage(filterCourses, member, historySearchRequestDto)))
                .collect(Collectors.toList());
    }

    // 내가 기록한 코스 중 검색 조건에 맞는 코스만 필터링
    public List<Course> makeFilterCourses(Member member, Long courseId, HistorySearchRequestDto historySearchRequestDto) {
        return courseCustomRepository.findAllByCatgegoryAndWriter(
            courseId,
            member,
            historySearchRequestDto.getPlaceCategories(),
            historySearchRequestDto.getTransCategories(),
            historySearchRequestDto.getWithCategories(),
            historySearchRequestDto.getRegions());
    }

    // 마지막 검색페이지 판별(TODO -> PageDTO 클래스 따로파서 static method로)
    public Boolean isSearchFinalPage(List<Course> filterCourses, Member member, HistorySearchRequestDto historySearchRequestDto) {
        return filterCourses.size() - 1 != -1 && courseCustomRepository.findAllByCatgegoryAndWriter(
            filterCourses.get(filterCourses.size() - 1).getCourseId(),
            member,
            historySearchRequestDto.getPlaceCategories(),
            historySearchRequestDto.getTransCategories(),
            historySearchRequestDto.getWithCategories(),
            historySearchRequestDto.getRegions()).isEmpty();
    }
}
