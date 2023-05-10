package com.team6.sole.domain.home;

import com.team6.sole.domain.follow.FollowRepository;
import com.team6.sole.domain.follow.model.FollowStatus;
import com.team6.sole.domain.home.dto.*;
import com.team6.sole.domain.home.entity.*;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.home.repository.*;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.scrap.ScrapFolderRespository;
import com.team6.sole.domain.scrap.entity.ScrapFolder;
import com.team6.sole.global.config.s3.AwsS3ServiceImpl;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.BadRequestException;
import com.team6.sole.global.error.exception.NotFoundException;
import com.team6.sole.infra.direction.DirectionService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
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

import static com.team6.sole.infra.direction.DirectionService.calculateDistance;

@Service
@Slf4j
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
    private final WebClient webClient;
    private final DirectionService directionService;

    @Value("${GOOGLE.APP_KEY}")
    private String APP_KEY;
    
    // 현재 위치 설정
    @Transactional
    public GpsResponseDto setCurrentGps(Member member, GpsReqeustDto gpsRequestDto) {
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
    public String showCurrentGps(Member member) {
        return makeShortenAddress(member.getCurrentGps().getAddress());
    }
    
    // 인기 코스 보기 테스트
    @Transactional(readOnly = true)
    public List<RecommendCourseResponseDto> showRecommendTest(Member member) {
        List<Course> recommendCourses = directionService.buildCourses(member.getCurrentGps());

        return recommendCourses.stream()
                .map(RecommendCourseResponseDto::of)
                .collect(Collectors.toList());
    }

    // 인기 코스 추천(7개 fix)
    @Transactional(readOnly = true)
    public List<RecommendCourseResponseDto> showRecommendCourses(Member member) {
        return member.getRecommendCourses().stream()
                .map(RecommendCourseResponseDto::of)
                .collect(Collectors.toList());
    }

    // 홈 보기(5개 + 5n)
    @Transactional(readOnly = true)
    public List<HomeResponseDto> showHomes(Member member, Long courseId) {
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
        boolean finalPage = courses.size() - 1 != -1
                && courseCustomRepository.findAllByCategory(courses.get(courses.size() - 1).getCourseId(),
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

    // 홈 검색(10개 + 10n)
    @Transactional(readOnly = true)
    public List<HomeResponseDto> searchHomes(Member member, Long courseId, String searchWord) {
        // 검색어로 코스 찾기
        List<Course> searchCourses = courseCustomRepository.findAllByTitleContaining(courseId, searchWord);
        boolean finalPage = searchCourses.size() - 1 != -1 && courseCustomRepository.findAllByTitleContaining(
                searchCourses.get(searchCourses.size() - 1).getCourseId(),
                searchWord).isEmpty();

        return searchCourses.stream()
                .map(course -> HomeResponseDto.of(
                        course,
                        // 스크랩 여부
                        courseMemberRepository.existsByMemberAndCourse_CourseId(
                                member,
                                course.getCourseId()),
                        // 마지막 페이지여부
                        finalPage))
                .collect(Collectors.toList());
    }
    
    // 코스 등록
    @SneakyThrows
    @Transactional
    public CourseResponseDto makeCourse(Member writer, CourseRequestDto courseRequestDto,
                                        Map<String, List<MultipartFile>> courseImagesMap) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        log.info(String.valueOf(courseImagesMap.size()));

        // 장소별 위, 경도 가져오기
        List<Gps> locations = courseRequestDto.getPlaceRequestDtos().stream()
                .map(placeRequestDto -> Gps.builder()
                        .latitude(placeRequestDto.getLatitude())
                        .longitude(placeRequestDto.getLongitude())
                        .build())
                .collect(Collectors.toList());
        
        // 코스 최단거리 합 계산
        double totalDistance = 0;
        for (int i = 0; i < locations.size(); i++) {
            if (i == locations.size() - 1) {
                break;
            }
            Gps start = locations.get(i);
            Gps end = locations.get(i + 1);
            totalDistance += calculateDistance(start.getLatitude(), start.getLongitude(),
                    end.getLatitude(), end.getLongitude());
        }

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
                .distance(totalDistance)
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
    public CourseDetailResponseDto showCourseDetail(Long courseId, Member member) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));

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
            if (placeUpdateRequestDto.getPlaceId() == null) {
                Place place = Place.builder()
                        .placeName(placeUpdateRequestDto.getPlaceName())
                        .duration(placeUpdateRequestDto.getDuration())
                        .description(placeUpdateRequestDto.getDescription())
                        .gps(
                                Gps.builder()
                                        .address(placeUpdateRequestDto.getAddress())
                                        .latitude(placeUpdateRequestDto.getLatitude())
                                        .longitude(placeUpdateRequestDto.getLongitude())
                                        .build())
                        .placeImgUrls(placeImages.get(placeUpdateRequestDto.getPlaceName()) == null
                                ? null
                                : awsS3Service.uploadImage(placeImages.get(placeUpdateRequestDto.getPlaceName()), "place"))
                        .course(course)
                        .build();
                placeRepository.saveAndFlush(place);
                course.putPlace(place);
            } else {
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
                        placeImages.get(placeUpdateRequestDto.getPlaceName()) == null
                                ? placeUpdateRequestDto.getPlaceImgUrls()
                                : Stream.of(
                                        awsS3Service.uploadImage(placeImages.get(placeUpdateRequestDto.getPlaceName()), "place"),
                                        placeUpdateRequestDto.getPlaceImgUrls())
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()));
            }
        }

        // 장소별 위, 경도 가져오기
        List<Gps> locations = courseUpdateRequestDto.getPlaceUpdateRequestDtos().stream()
                .map(placeRequestDto -> Gps.builder()
                        .latitude(placeRequestDto.getLatitude())
                        .longitude(placeRequestDto.getLongitude())
                        .build())
                .collect(Collectors.toList());

        // 코스 최단거리 합 계산
        double totalPlaceDistance = 0;
        for (int i = 0; i < locations.size(); i++) {
            if (i == locations.size() - 1) {
                break;
            }
            Gps start = locations.get(i);
            Gps end = locations.get(i + 1);
            totalPlaceDistance += calculateDistance(start.getLatitude(), start.getLongitude(),
                    end.getLatitude(), end.getLongitude());
        }

        // 코스
        course.modCourse(
                courseUpdateRequestDto.getTitle(),
                formatter.parse(courseUpdateRequestDto.getStartDate()),
                course.getPlaces().stream()
                        .mapToInt(Place::getDuration)
                        .sum(),
                totalPlaceDistance,
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
    public synchronized void scrapCourse(Member member, Long courseId, Long scrapFolderId) {
        // Optional로 이미 스크랩되어있는지 확인
        Optional<CourseMember> checkCourseMember = courseMemberRepository.findByCourse_CourseIdAndMember(courseId, member);

        // 이미 스크랩되어있다면 취소, 아니면 스크랩
        if (checkCourseMember.isPresent()) {
            courseMemberRepository.deleteByCourse_CourseIdAndMember(courseId, member);
            checkCourseMember.get().getCourse().removeScrapCount();
        } else {
            ScrapFolder scrapFolder = scrapFolderRespository.findById(scrapFolderId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.SCRAP_FOLDER_NOT_FOUND));

            CourseMember courseMember = CourseMember.builder()
                    .course(courseRepository.findById(courseId)
                            .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND)))
                    .member(member)
                    .scrapFolder(scrapFolder)
                    .build();
            courseMemberRepository.saveAndFlush(courseMember);

            courseMember.getCourse().addScrapCount();
            courseRepository.saveAndFlush(courseMember.getCourse());
        }
    }
    
    // 코스 신고하기
    @Transactional
    public String declareCourse(Member member, Long courseId) {
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
    public FavCategoryResponseDto showFavCategory(Member member) {
        return FavCategoryResponseDto.of(member);
    }

    // 선호 카테고리 수정
    @Transactional
    public FavCategoryResponseDto modFavCategory(Member member, FavCategoryRequestDto favCategoryRequestDto) {
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

    // 지역명 필터링
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
        else if (addressArr[0].equals("충청북도")
                || addressArr[0].equals("충청남도")
                || addressArr[0].equals("전라북도")
                || addressArr[0].equals("전라남도")
                || addressArr[0].equals("경상북도")
                || addressArr[0].equals("경상남도")) {
            shortenAddress =  addressArr[0].charAt(0) + addressArr[0].charAt(2) + " " + addressArr[1];
        } else {
            shortenAddress = addressArr[0];
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
                            .uri(getAdressURL, builder -> builder
                                    .queryParam("latlng", latitude + "," + longitude)
                                    .queryParam("language", "ko")
                                    .queryParam("key", APP_KEY)
                                    .build())
                            .retrieve()
                            .bodyToMono(GeocodeResponseDto.class)
                            .block()).getResults()[0].getFormatted_address();
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequestException(ErrorCode.GOOGLE_BAD_REQUEST);
        }
    }
}
