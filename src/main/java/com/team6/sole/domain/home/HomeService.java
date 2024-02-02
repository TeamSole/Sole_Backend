package com.team6.sole.domain.home;

import com.team6.sole.domain.follow.FollowRepository;
import com.team6.sole.domain.follow.model.FollowStatus;
import com.team6.sole.domain.home.dto.*;
import com.team6.sole.domain.home.entity.*;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.home.model.PlaceCategory;
import com.team6.sole.domain.home.model.Region;
import com.team6.sole.domain.home.model.TransCategory;
import com.team6.sole.domain.home.model.WithCategory;
import com.team6.sole.domain.home.repository.*;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.scrap.ScrapFolderRespository;
import com.team6.sole.domain.scrap.entity.ScrapFolder;
import com.team6.sole.global.config.s3.AwsS3ServiceImpl;
import com.team6.sole.global.config.security.oauth.GoogleUtils;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.NotFoundException;
import com.team6.sole.infra.direction.DirectionService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.team6.sole.domain.home.utils.RegionUtils.makeRegion;
import static com.team6.sole.domain.home.utils.RegionUtils.makeShortenAddress;
import static com.team6.sole.infra.direction.DirectionService.calculateDistance;

@Service
@RequiredArgsConstructor
public class HomeService {
    private final ScrapFolderRespository scrapFolderRespository;
    private final CourseMemberRepository courseMemberRepository;
    private final FollowRepository followRepository;
    private final CourseRepository courseRepository;
    private final CourseCustomRepository courseCustomRepository;
    private final PlaceRepository placeRepository;
    private final DeclarationRepository declarationRepository;
    private final AwsS3ServiceImpl awsS3Service;
    private final GoogleUtils googleUtils;
    private final DirectionService directionService;

    public static final String COURSE = "course";
    public static final String PLACE = "place";
    public static final String THUMBNAIL_IMG = "thumbnailImg";
    
    // 현재 위치 설정
    @Transactional
    public GpsResponseDto setCurrentGps(Member member, GpsReqeustDto gpsRequestDto) {
        String rawAddress = googleUtils.convertAdress(gpsRequestDto.getLatitude(), gpsRequestDto.getLongitude());

        Gps gps = GpsReqeustDto.gpsToEntity(makeRawAddressToAddress(rawAddress), gpsRequestDto);

        member.setCurrentGps(gps);

        return GpsResponseDto.of(member);
    }

    public String makeRawAddressToAddress(String rawAddress) {
        int idx = rawAddress.indexOf("국");

        return rawAddress.substring(idx + 2);
    }

    // 현재 위치 보기
    @Transactional(readOnly = true)
    public String showCurrentGps(Member member) {
        return makeShortenAddress(member.getCurrentGps().getAddress());
    }
    
    // 인기 코스 보기 테스트
    @Transactional(readOnly = true)
    public List<RecommendCourseResponseDto> showRecommendTest(Member member) {
        List<Course> recommendCourses = directionService.buildCourses(member.getCurrentGps());

        return RecommendCourseResponseDto.of(recommendCourses);
    }

    // 인기 코스 추천(7개 fix)
    @Transactional(readOnly = true)
    public List<RecommendCourseResponseDto> showRecommendCourses(Member member) {
        return RecommendCourseResponseDto.of(member.getRecommendCourses());
    }

    // 홈 보기(5개 + 5n)
    @Transactional(readOnly = true)
    public List<HomeResponseDto> showHomes(Member member, Long courseId) {
        if (member.getFavoriteCategory().getTransCategories().isEmpty()
                && member.getFavoriteCategory().getPlaceCategories().isEmpty()
                && member.getFavoriteCategory().getWithCategories().isEmpty()) {
            return Collections.emptyList();
        }

        List<Course> courses = findAllByCategory(courseId, member);

        return showHomeCourses(courses, member);
    }

    // 선호하는 카테고리 findAll
    public List<Course> findAllByCategory(Long courseId, Member member) {
        return courseCustomRepository.findAllByCategory(
                    courseId,
                    member.getFavoriteCategory().getPlaceCategories(),
                    member.getFavoriteCategory().getWithCategories(),
                    member.getFavoriteCategory().getTransCategories());
    }

    // 마지막 페이지 판별(TODO -> PageDTO 클래스 따로파서 static method로)
    public Boolean isFinalPage(List<Course> courses, Member member) {
        return courses.size() - 1 != -1 
                && courseCustomRepository.findAllByCategory(courses.get(courses.size() - 1).getCourseId(),
                member.getFavoriteCategory().getPlaceCategories(),
                member.getFavoriteCategory().getWithCategories(),
                member.getFavoriteCategory().getTransCategories()).isEmpty();
    }

    // 팔로잉한 사람들의 코스들 중 스크랩한 코스(T/F)
    public Boolean isCourseScrap(Member member, Long courseId) {
        return courseMemberRepository.existsByMemberAndCourse_CourseId(member, courseId);
    }

    // 선호 코스 모아보기
    public List<HomeResponseDto> showHomeCourses(List<Course> courses, Member member) {
        return courses.stream()
                .map(course -> HomeResponseDto.of(
                        course,
                        isCourseScrap(member, course.getCourseId()),
                        isFinalPage(courses, member)))
                .collect(Collectors.toList());
    }

    // 홈 검색(10개 + 10n)
    @Transactional(readOnly = true)
    public List<HomeResponseDto> searchHomes(Member member, Long courseId, String searchWord,
                                             Set<PlaceCategory> placeCategories, Set<TransCategory> transCategories, Set<WithCategory> withCategories,
                                             List<Region> regions) {
        // 검색어로 코스 찾기
        List<Course> searchCourses = courseCustomRepository.findAllByTitleContaining(courseId, searchWord, placeCategories, transCategories, withCategories, regions);

        Boolean isFinalPage = isFinalPage(searchCourses, searchWord, placeCategories, transCategories, withCategories, regions);

        return showSearchedCourses(searchCourses, member, isFinalPage);
    }

    // 마지막 페이지 판별(TODO -> PageDTO 클래스 따로파서 static method로)
    public Boolean isFinalPage(List<Course> searchCourses, String searchWord, 
                                Set<PlaceCategory> placeCategories, Set<TransCategory> transCategories, Set<WithCategory> withCategories,
                                List<Region> regions) {
        return searchCourses.size() - 1 != -1 
                && courseCustomRepository.findAllByTitleContaining(
            searchCourses.get(searchCourses.size() - 1).getCourseId(),
            searchWord, placeCategories, transCategories, withCategories, regions).isEmpty();
    }

    // 선호 코스 모아보기(검색 포함)
    public List<HomeResponseDto> showSearchedCourses(List<Course> courses, Member member, Boolean isFinalPage) {
        return courses.stream()
                .map(course -> HomeResponseDto.of(
                        course,
                        isCourseScrap(member, course.getCourseId()),
                        isFinalPage))
                .collect(Collectors.toList());
    }

    // 코스 등록
    @SneakyThrows
    @Transactional
    public CourseResponseDto makeCourse(Member writer, CourseRequestDto courseRequestDto,
                                        Map<String, List<MultipartFile>> courseImagesMap) {
        List<Gps> locations = showLatAndLongsByCourses(courseRequestDto);
        double totalDistance = caculateTotalDistance(locations);

        String thumbnailUrl = checkThumbnailUrl(courseImagesMap);
        Date startDate = convertStringToDate(courseRequestDto.getDate());
        Region region = makeRegion(makeShortenAddress(courseRequestDto.getPlaceRequestDtos().get(0).getAddress()));
        int duration = makeDuration(courseRequestDto);

        Course course = CourseRequestDto.courseToEntity(thumbnailUrl, totalDistance, startDate, region, duration, writer, courseRequestDto);
        courseRepository.saveAndFlush(course);

        // 장소 저장
        for (PlaceRequestDto placeRequestDto : courseRequestDto.getPlaceRequestDtos()) {
            Place place = PlaceRequestDto.placeToEntity(checkPlaceImgUrls(courseImagesMap, placeRequestDto), placeRequestDto, course);
            placeRepository.save(place);
            course.putPlace(place);
        }

        return CourseResponseDto.of(course);
    }

    // 장소별 위, 경도 가져오기
    public List<Gps> showLatAndLongsByCourses(CourseRequestDto courseRequestDto) {
        return courseRequestDto.getPlaceRequestDtos().stream()
                .map(placeRequestDto -> Gps.builder()
                        .latitude(placeRequestDto.getLatitude())
                        .longitude(placeRequestDto.getLongitude())
                        .build())
                .collect(Collectors.toList());
    }

    // 코스 최단거리 합 계산
    public Double caculateTotalDistance(List<Gps> locations) {
        double totalDistance = 0.0;
        for (int i = 0; i < locations.size(); i++) {
            if (i == locations.size() - 1) {
                break;
            }
            Gps start = locations.get(i);
            Gps end = locations.get(i + 1);
            totalDistance += calculateDistance(start.getLatitude(), start.getLongitude(),
                    end.getLatitude(), end.getLongitude());
        }

        return totalDistance;
    }

    public String checkThumbnailUrl(Map<String, List<MultipartFile>> courseImagesMap) {
        return courseImagesMap.get(THUMBNAIL_IMG) == null
                ? null
                : awsS3Service.uploadImage(courseImagesMap.get(THUMBNAIL_IMG).get(0), COURSE);
    }

    // 시작날짜 String -> Date(형변환)
    public Date convertStringToDate(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        return formatter.parse(date);
    }

    // 장소간 이동시간 구하기
    public int makeDuration(CourseRequestDto courseRequestDto) {
        return courseRequestDto.getPlaceRequestDtos().stream()
                .mapToInt(PlaceRequestDto::getDuration) 
                .sum();
    }

    public List<String> checkPlaceImgUrls(Map<String, List<MultipartFile>> courseImagesMap, PlaceRequestDto placeRequestDto) {
        return courseImagesMap.get(placeRequestDto.getPlaceName()) == null
                ? null
                : awsS3Service.uploadImage(courseImagesMap.get(placeRequestDto.getPlaceName()), PLACE);
    }
    
    // 코스 상세 조회
    @Transactional(readOnly = true)
    public CourseDetailResponseDto showCourseDetail(Long courseId, Member member) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));

        boolean checkWriter = isWriter(course, member);

        FollowStatus followStatus = checkFollowStatus(course, member);

        return CourseDetailResponseDto.of(course, checkWriter, followStatus);
    }

    public Boolean isWriter(Course course, Member member) {
        return course.getWriter().getMemberId().equals(member.getMemberId());
    }

    public FollowStatus checkFollowStatus(Course course, Member member) {
        return followRepository.existsByFromMember_MemberIdAndToMember_MemberId(member.getMemberId(), course.getWriter().getMemberId())
                ? FollowStatus.FOLLOWING
                : FollowStatus.NOT_FOLLOW;
    }

    // 코스 업데이트
    @SneakyThrows
    @Transactional
    public CourseResponseDto modCourse(Long courseId,
                                       Map<String, List<MultipartFile>> placeImages,
                                       CourseUpdateRequestDto courseUpdateRequestDto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));

        // 장소 업데이트
        for (PlaceUpdateRequestDto placeUpdateRequestDto : courseUpdateRequestDto.getPlaceUpdateRequestDtos()) {
            if (placeUpdateRequestDto.getPlaceId() == null) {
                Place place = PlaceRequestDto.updatePlaceToEntity(checkPlaceImages(placeImages, placeUpdateRequestDto), placeUpdateRequestDto, course);
                placeRepository.saveAndFlush(place);
                course.putPlace(place);
            } else {
                Place place = placeRepository.findById(placeUpdateRequestDto.getPlaceId())
                        .orElseThrow(() -> new NotFoundException(ErrorCode.PLACE_NOT_FOUND));

                place.modPlace(placeUpdateRequestDto, PlaceUpdateRequestDto.updateGpsToEntity(placeUpdateRequestDto));
                place.modPlaceImgUrls(mergePlaceImgUrlsAndUpdateImgUrls(placeImages, placeUpdateRequestDto));
            }
        }

        List<Gps> locations = showLatAndLongsByUpdatedCourses(courseUpdateRequestDto);
        double totalPlaceDistance = caculateTotalDistance(locations);
        Date startDate = convertStringToDate(courseUpdateRequestDto.getStartDate());
        int totalDuration = calculateTotalDuration(course);
        Region region = makeRegion(makeShortenAddress(courseUpdateRequestDto.getPlaceUpdateRequestDtos().get(0).getAddress()));

        // 코스 업데이트
        course.modCourse(courseUpdateRequestDto, startDate, totalDuration, totalPlaceDistance, region);
        course.modThumbnailImg(checkUpdatedThumbnailImg(course, placeImages));

        return CourseResponseDto.of(course);
    }

    public List<String> checkPlaceImages(Map<String, List<MultipartFile>> placeImages, PlaceUpdateRequestDto placeUpdateRequestDto) {
        return placeImages.get(placeUpdateRequestDto.getPlaceName()) == null
                ? null
                : awsS3Service.uploadImage(placeImages.get(placeUpdateRequestDto.getPlaceName()), PLACE);
    }

    public List<String> mergePlaceImgUrlsAndUpdateImgUrls(Map<String, List<MultipartFile>> placeImages, PlaceUpdateRequestDto placeUpdateRequestDto) {
        return Stream.of(
            awsS3Service.uploadImage(placeImages.get(placeUpdateRequestDto.getPlaceName()), PLACE),
            placeUpdateRequestDto.getPlaceImgUrls())
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    public List<String> makeUpdatedPlaceImgUrls(Map<String, List<MultipartFile>> placeImages, PlaceUpdateRequestDto placeUpdateRequestDto) {
        return placeImages.get(placeUpdateRequestDto.getPlaceName()) == null
                ? placeUpdateRequestDto.getPlaceImgUrls()
                : mergePlaceImgUrlsAndUpdateImgUrls(placeImages, placeUpdateRequestDto);
    }

    // 장소별 위, 경도 가져오기
    public List<Gps> showLatAndLongsByUpdatedCourses(CourseUpdateRequestDto courseUpdateRequestDto) {
        return courseUpdateRequestDto.getPlaceUpdateRequestDtos().stream()
                .map(placeRequestDto -> Gps.builder()
                        .latitude(placeRequestDto.getLatitude())
                        .longitude(placeRequestDto.getLongitude())
                        .build())
                .collect(Collectors.toList());
    }

    public int calculateTotalDuration(Course course) {
        return course.getPlaces().stream()
            .mapToInt(Place::getDuration)
            .sum();
    }

    public String checkUpdatedThumbnailImg(Course course, Map<String, List<MultipartFile>> placeImages) {
        return placeImages.get(THUMBNAIL_IMG) == null
                ? course.getThumbnailUrl()
                : awsS3Service.uploadImage(placeImages.get(THUMBNAIL_IMG).get(0), COURSE);
    }

    // 코스 삭제
    @Transactional
    public void delCourse(Long courseId) {
        courseRepository.deleteById(courseId);
    }

    // 코스 스크랩 및 취소
    @Async("home")
    @Transactional
    public synchronized void scrapCourse(Member member, Long courseId, Long scrapFolderId) {
        // Optional로 이미 스크랩되어있는지 확인
        Optional<CourseMember> checkCourseMember = courseMemberRepository.findByCourse_CourseIdAndMember(courseId, member);

        // 이미 스크랩되어있다면 취소, 아니면 스크랩
        if (checkCourseMember.isPresent()) {
            unscrap(courseId, member, checkCourseMember.get());
        } else {
            ScrapFolder scrapFolder = scrapFolderRespository.findById(scrapFolderId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.SCRAP_FOLDER_NOT_FOUND));
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));

            CourseMember courseMember = CourseRequestDto.courseMemberToEntity(course, member, scrapFolder);
            courseMemberRepository.saveAndFlush(courseMember);

            scrap(courseMember);
        }
    }

    public void unscrap(Long courseId, Member member, CourseMember courseMember) {
        courseMemberRepository.deleteByCourse_CourseIdAndMember(courseId, member);
        courseMember.getCourse().removeScrapCount();
    }

    public void scrap(CourseMember courseMember) {
        courseMember.getCourse().addScrapCount();
        courseRepository.saveAndFlush(courseMember.getCourse());
    }
    
    // 코스 신고하기
    @Transactional
    public String declareCourse(Member member, Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));

        Declaration declaration = CourseRequestDto.declarationToEntity(course, member);
        declarationRepository.save(declaration);

        return "신고가 접수되었습니다.";
    }

    // 선호 카테고리 보기
    @Transactional(readOnly = true)
    public FavCategoryResponseDto showFavCategory(Member member) {
        return FavCategoryResponseDto.of(member);
    }

    // 선호 카테고리 수정
    @Transactional
    public FavCategoryResponseDto modFavCategory(Member member, FavCategoryRequestDto favCategoryRequestDto) {
        Category favCategory = CourseRequestDto.categoriesToEntity(favCategoryRequestDto);

        member.modFavCategory(favCategory);

        return FavCategoryResponseDto.of(member);
    }

    // 이미지 업로드 테스트
    @Transactional
    public String imageTest(Map<String, List<MultipartFile>> fileMap) {
        awsS3Service.uploadImage(fileMap.get(THUMBNAIL_IMG).get(0), COURSE);

        return "성공";
    }
}
