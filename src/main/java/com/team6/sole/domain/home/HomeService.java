package com.team6.sole.domain.home;

import com.team6.sole.domain.follow.FollowRepository;
import com.team6.sole.domain.follow.model.FollowStatus;
import com.team6.sole.domain.home.dto.*;
import com.team6.sole.domain.home.entity.*;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.home.repository.*;
import com.team6.sole.domain.member.MemberRepository;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.global.config.s3.AwsS3ServiceImpl;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.BadRequestException;
import com.team6.sole.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final DeclarationRepository declarationRepository;
    private final AwsS3ServiceImpl awsS3Service;
    private final WebClient webClient;

    @Value("${GOOGLE.APP_KEY}")
    private String APP_KEY;
    
    // 현재 위치 설정
    @Transactional
    public GpsResponseDto setCurrentGps(String socialId, GpsReqeustDto gpsRequestDto) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        String address = convertAdress(gpsRequestDto.getLatitude(), gpsRequestDto.getLongitude());
        int idx = address.indexOf("국");

        member.setCurrentGps(
                Gps.builder()
                        .address(address.substring(idx + 2))
                        .latitude(gpsRequestDto.getLatitude())
                        .longitude(gpsRequestDto.getLongitude())
                        .build());

        return GpsResponseDto.of(member);
    }

    // 현재 위치 보기
    @Transactional(readOnly = true)
    public String showCurrentGps(String socialId) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return makeShortenAddress(member.getCurrentGps().getAddress());
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

        if (member.getFavoriteCategory().getTransCategories().isEmpty()
                && member.getFavoriteCategory().getPlaceCategories().isEmpty()
                && member.getFavoriteCategory().getWithCategories().isEmpty()) {
            return Collections.emptyList();
        }

        // 선호하는 카테고리 findAll
        List<Course> courses = courseCustomRepository
                .findAllByCategory(
                        courseId,
                        member.getFavoriteCategory().getPlaceCategories(),
                        member.getFavoriteCategory().getWithCategories(),
                        member.getFavoriteCategory().getTransCategories());
        boolean finalPage = courseCustomRepository.findAllByCategory(courses.get(courses.size() - 1).getCourseId(),
                member.getFavoriteCategory().getPlaceCategories(),
                member.getFavoriteCategory().getWithCategories(),
                member.getFavoriteCategory().getTransCategories()).isEmpty();

        // 좋아요 여부 추가 및 dto 변환
        return courses.stream()
                .map(course -> HomeResponseDto.of(
                        course,
                        courseMemberRepository.existsByMemberAndCourse_CourseId(member, course.getCourseId()),
                        finalPage))
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
                        // 스크랩 여부
                        courseMemberRepository.existsByMemberAndCourse_CourseId(
                                member,
                                course.getCourseId()),
                        // 마지막 페이지여부
                        courseCustomRepository.findAllByTitleContaining(
                                searchCourses.get(searchCourses.size() - 1).getCourseId(),
                                searchWord).isEmpty()))
                .collect(Collectors.toList());
    }
    
    // 코스 등록
    @SneakyThrows
    @Transactional
    public CourseResponseDto makeCourse(String socialId, CourseRequestDto courseRequestDto,
                                        Map<String, List<MultipartFile>> courseImagesMap) {
        Member writer = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        // 코스 저장
        Course course = Course.builder()
                .thumbnailUrl(courseImagesMap.get("thumbnailImg") == null
                        ? null
                        : awsS3Service.uploadImage(courseImagesMap.get("thumbnailImg").get(0), "course"))
                .scrapCount(0)
                .title(courseRequestDto.getTitle())
                .description(courseRequestDto.getDescription())
                .startDate(formatter.parse(courseRequestDto.getDate()))
                .duration(courseRequestDto.getPlaceRequestDtos().stream()
                        .mapToInt(PlaceRequestDto::getDuration)
                        .sum())
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
                    .duration(placeRequestDto.getDuration())
                    .description(placeRequestDto.getDescription())
                    .gps(
                            Gps.builder()
                                    .address(placeRequestDto.getAddress())
                                    .latitude(placeRequestDto.getLatitude())
                                    .longitude(placeRequestDto.getLongitude())
                                    .build())
                    .placeImgUrls(courseImagesMap.get(placeRequestDto.getPlaceName()) == null
                            ? null
                            : awsS3Service.uploadImage(courseImagesMap.get(placeRequestDto.getPlaceName()), "place"))
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

        boolean checkWriter = course.getWriter().getMemberId().equals(member.getMemberId());

        FollowStatus followStatus = followRepository.existsByFromMember_MemberIdAndToMember_MemberId(member.getMemberId(), course.getWriter().getMemberId())
                ? FollowStatus.FOLLOWING
                : FollowStatus.NOT_FOLLOW;

        return CourseDetailResponseDto.of(course, checkWriter, followStatus);
    }

    // 코스 업데이트
    @SneakyThrows
    @Transactional
    public CourseResponseDto modCourse(Long courseId,
                                       Map<String, List<MultipartFile>> placeImages,
                                       CourseUpdateRequestDto courseUpdateRequestDto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        // 장소
        for (PlaceUpdateRequestDto placeUpdateRequestDto : courseUpdateRequestDto.getPlaceUpdateRequestDtos()) {
            Place place = placeRepository.findById(placeUpdateRequestDto.getPlaceId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.PLACE_NOT_FOUND));
            place.modPlace(
                    placeUpdateRequestDto.getPlaceName(),
                    placeUpdateRequestDto.getDuration(),
                    placeUpdateRequestDto.getDescription(),
                    Gps.builder()
                            .address(placeUpdateRequestDto.getAddress())
                            .latitude(placeUpdateRequestDto.getLatitude())
                            .longitude(placeUpdateRequestDto.getLongitude())
                            .distance(0.0)
                            .build());

            place.modPlaceImgUrls(
                    placeImages.get(placeUpdateRequestDto.getPlaceId().toString()) == null
                            ? placeUpdateRequestDto.getPlaceImgUrls()
                            : Stream.of(
                                    placeUpdateRequestDto.getPlaceImgUrls(), awsS3Service.uploadImage(placeImages.get(placeUpdateRequestDto.getPlaceId()), "place"),
                                    placeUpdateRequestDto.getPlaceImgUrls())
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList()));
        }

        // 코스
        course.modCourse(
                courseUpdateRequestDto.getTitle(),
                formatter.parse(courseUpdateRequestDto.getStartDate()),
                course.getPlaces().stream()
                        .mapToInt(Place::getDuration)
                        .sum(),
                courseUpdateRequestDto.getPlaceCategories(),
                courseUpdateRequestDto.getWithCategories(),
                courseUpdateRequestDto.getTransCategories(),
                courseUpdateRequestDto.getDescription());
        course.modThumbnailImg(
                placeImages.get("thumbnailImg") == null
                        ? course.getThumbnailUrl()
                        : awsS3Service.uploadImage(placeImages.get("thumbnailImg").get(0), "course"));

        return CourseResponseDto.of(course);
    }

    // 코스 삭제
    @Transactional
    public void delCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));
        courseRepository.delete(course);
    }
    
    // 코스 스크랩 및 취소
    @Async("home")
    @Transactional
    public synchronized void scrapCourse(String socialId, Long courseId) {
        Optional<CourseMember> checkCourseMember = courseMemberRepository.findByMember_SocialIdAndCourse_CourseId(socialId, courseId);

        if (checkCourseMember.isPresent()) {
            courseMemberRepository.deleteByMember_SocialIdAndCourse_CourseId(socialId, courseId);
            checkCourseMember.get().getCourse().removeScrapCount();
            courseRepository.saveAndFlush(checkCourseMember.get().getCourse());
        } else {
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
    }
    
    // 코스 신고하기
    @Transactional
    public String declareCourse(String socialId, Long courseId) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));

        Declaration declaration = Declaration.builder()
                .course(course)
                .member(member)
                .build();
        declarationRepository.save(declaration);

        return "신고가 접수되었습니다.";
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

    // 이미지 업로드 테스트
    @Transactional
    public String imageTest(Map<String, List<MultipartFile>> fileMap) {
        awsS3Service.uploadImage(fileMap.get("thumbnailImg").get(0), "course");

        return "성공";
    }

    public static String makeShortenAddress(String address) {
        String[] addressArr = address.split(" ");
        String shortenAddress = "";
        if (addressArr[0].equals("서울특별시")
                || addressArr[0].equals("부산광역시")
                || addressArr[0].equals("대구광역시")
                || addressArr[0].equals("인천광역시")
                || addressArr[0].equals("대전광역시")
                || addressArr[0].equals("울산광역시")
                || addressArr[0].equals("광주광역시")
                || addressArr[0].equals("세종특별자치시")
                || addressArr[0].equals("제주특별자치도")) {
            shortenAddress = addressArr[0].substring(0, 2) + " " + addressArr[1];
        }
        else if (addressArr[0].equals("경기도")
                || addressArr[0].equals("강원도")) {
            shortenAddress = addressArr[0].substring(0, 2) + " " + addressArr[1];
        }
        else {
            shortenAddress =  addressArr[0].charAt(0) + addressArr[0].charAt(2) + " " + addressArr[1];
        }

        return shortenAddress;
    }

    // 현재 위치 좌표 -> 주소 변환
    @SneakyThrows
    public String convertAdress(Double latitude, Double longitude) {
        String getAdressURL = "https://maps.googleapis.com/maps/api/geocode/json";

        try {
            return Objects.requireNonNull(webClient
                            .get()
                            .uri(getAdressURL, builder -> {
                                return builder
                                        .queryParam("latlng", latitude + "," + longitude)
                                        .queryParam("language", "ko")
                                        .queryParam("key", APP_KEY)
                                        .build();
                            })
                            .retrieve()
                            .bodyToMono(GeocodeResponseDto.class)
                            .block()).getResults()[0].getFormatted_address();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(ErrorCode.GOOGLE_BAD_REQUEST);
        }
    }
}
