package com.team6.sole.domain.home;

import com.team6.sole.domain.follow.FollowRepository;
import com.team6.sole.domain.follow.model.FollowStatus;
import com.team6.sole.domain.home.dto.*;
import com.team6.sole.domain.home.entity.Category;
import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.Gps;
import com.team6.sole.domain.home.entity.Place;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.home.repository.CourseCustomRepository;
import com.team6.sole.domain.home.repository.CourseMemberRepository;
import com.team6.sole.domain.home.repository.CourseRepository;
import com.team6.sole.domain.home.repository.PlaceRepository;
import com.team6.sole.domain.member.MemberRepository;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.global.config.s3.AwsS3ServiceImpl;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HomeService {
    private final CourseMemberRepository courseMemberRepository;
    private final FollowRepository followRepository;
    private final CourseRepository courseRepository;
    private final CourseCustomRepository courseCustomRepository;
    private final PlaceRepository placeRepository;
    private final MemberRepository memberRepository;
    private final AwsS3ServiceImpl awsS3Service;
    
    // 현재 위치 설정
    @Transactional
    public GpsResponseDto setCurrentGps(String socialId, GpsReqeustDto gpsRequestDto) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        member.setCurrentGps(gpsRequestDto.getGps());

        return GpsResponseDto.of(member);
    }

    // 인기 코스 추천(7개 fix)
    @Transactional(readOnly = true)
    @Cacheable(value = "recommends")
    public List<RecommendCourseResponseDto> showRecommendCourses(String socialId) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return member.getRecommendCourses().stream()
                .map(RecommendCourseResponseDto::of)
                .collect(Collectors.toList());
    }

    // 홈 보기(5개 + 5n)
    @Transactional(readOnly = true)
    public List<HomeResponseDto> showHomes(String socialId, Long courseId) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        // 선호하는 카테고리 findAll
        List<Course> courses = courseCustomRepository
                .findAllByCatgegory(
                        courseId,
                        member.getFavoriteCategory().getPlaceCategories(),
                        member.getFavoriteCategory().getWithCategories(),
                        member.getFavoriteCategory().getTransCategories());

        // 좋아요 여부 추가 및 dto 변환
        return courses.stream()
                .map(course -> HomeResponseDto.of(
                        course,
                        courseMemberRepository.existsByMemberAndCourse_CourseId(member, course.getCourseId())))
                .collect(Collectors.toList());
    }

    // 홈 검색(10개 +10n)
    @Transactional(readOnly = true)
    public List<HomeResponseDto> searchHomes(String socialId, Long courseId, String searchWord) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        // 검색어로 코스 찾기
        List<Course> searchCourses = courseCustomRepository.findAllByTitleContaining(courseId, searchWord);

        return searchCourses.stream()
                .map(course -> HomeResponseDto.of(
                        course,
                        courseMemberRepository.existsByMemberAndCourse_CourseId(
                                member,
                                course.getCourseId())))
                .collect(Collectors.toList());
    }
    
    // 코스 등록
    @SneakyThrows
    @Transactional
    public CourseResponseDto makeCourse(String socialId, CourseRequestDto courseRequestDto,
                                        MultipartFile thumbnailImg, Map<String, List<MultipartFile>> courseImagesMap) {
        Member writer = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        // 코스 저장
        Course course = Course.builder()
                .thumbnailUrl(thumbnailImg == null
                        ? null
                        : awsS3Service.uploadImage(thumbnailImg, "course"))
                .scrapCount(0)
                .title(courseRequestDto.getTitle())
                .description(courseRequestDto.getDescription())
                .startDate(formatter.parse(courseRequestDto.getDate()))
                .duration(courseRequestDto.getDuration())
                .distance(courseRequestDto.getDistance())
                .placeCategories(courseRequestDto.getPlaceCategories())
                .withCategories(courseRequestDto.getWithCategories())
                .transCategories(courseRequestDto.getTransCategories())
                .writer(writer)
                .places(new ArrayList<>())
                .build();
        courseRepository.saveAndFlush(course);

        // 장소 저장
        for (PlaceRequestDto placeRequestDto : courseRequestDto.getPlaceRequestDtos()) {
            Place place = Place.builder()
                    .placeName(placeRequestDto.getPlaceName())
                    .description(placeRequestDto.getDescription())
                    .gps(
                            Gps.builder()
                                    .address(placeRequestDto.getAddress())
                                    .latitude(placeRequestDto.getLatitude())
                                    .longitude(placeRequestDto.getLongitude())
                                    .build())
                    .placeImgUrls(courseImagesMap == null
                            ? null
                            : awsS3Service.uploadImage(courseImagesMap.get(placeRequestDto.getPlaceName()), "course"))
                    .course(course)
                    .build();
            placeRepository.save(place);
            course.putPlace(place);
        }

        return CourseResponseDto.of(course);
    }
    
    // 코스 상세 조회
    @Transactional(readOnly = true)
    public CourseDetailResponseDto showCourseDetail(Long courseId, String socialId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        boolean isWriter = course.getWriter().getMemberId().equals(member.getMemberId());

        FollowStatus followStatus = followRepository.existsByFromMember_MemberIdAndToMember_MemberId(member.getMemberId(), course.getWriter().getMemberId())
                ? FollowStatus.FOLLOWING
                : FollowStatus.NOT_FOLLOW;

        return CourseDetailResponseDto.of(course, isWriter, followStatus);
    }

    // 코스 삭제
    @Transactional
    public void delCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));
        courseRepository.delete(course);
    }
    
    // 코스 스크랩
    @Async("home")
    @Transactional
    public synchronized void scrapCourse(String socialId, Long courseId) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));

        CourseMember courseMember = CourseMember.builder()
                .member(member)
                .course(course)
                .build();
        courseMemberRepository.saveAndFlush(courseMember);

        course.addScrapCount();
        courseRepository.saveAndFlush(course);
    }

    // 코스 스크랩 취소
    @Async("home")
    @Transactional
    public synchronized void scrapCancelCourse(String socialId, Long courseId) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));

        courseMemberRepository.deleteByMemberAndCourse(member, course);
        course.removeScrapCount();
        courseRepository.saveAndFlush(course);
    }

    // 선호 카테고리 보기
    @Transactional(readOnly = true)
    public FavCategoryResponseDto showFavCategory(String socialId) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return FavCategoryResponseDto.of(member);
    }

    // 선호 카테고리 수정
    @Transactional
    public FavCategoryResponseDto modFavCategory(String socialId, FavCategoryRequestDto favCategoryRequestDto) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        member.modFavCategory(Category.builder()
                .placeCategories(favCategoryRequestDto.getPlaceCategories())
                .withCategories(favCategoryRequestDto.getWithCategories())
                .transCategories(favCategoryRequestDto.getTransCategories())
                .build());

        return FavCategoryResponseDto.of(member);
    }
}
