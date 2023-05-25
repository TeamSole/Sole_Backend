package com.team6.sole.domain.home;

import com.team6.sole.domain.follow.FollowRepository;
import com.team6.sole.domain.follow.model.FollowStatus;
import com.team6.sole.domain.home.dto.*;
import com.team6.sole.domain.home.entity.*;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.home.model.Region;
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
    public List<HomeResponseDto> searchHomes(Member member, Long courseId, String searchWord, List<Region> regions) {
        // 검색어로 코스 찾기
        List<Course> searchCourses = courseCustomRepository.findAllByTitleContaining(courseId, searchWord, regions);
        boolean finalPage = searchCourses.size() - 1 != -1 && courseCustomRepository.findAllByTitleContaining(
                searchCourses.get(searchCourses.size() - 1).getCourseId(),
                searchWord, regions).isEmpty();

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
                .region(makeRegion(
                        makeShortenAddress(courseRequestDto.getPlaceRequestDtos().get(0).getAddress())))
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
                makeRegion(makeShortenAddress(courseUpdateRequestDto.getPlaceUpdateRequestDtos().get(0).getAddress())),
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

    // 지역명(String) -> Region(Enum)
    public static Region makeRegion(String shortenAddress) {
        Region region = null;

        if (shortenAddress.startsWith("서울")) {
            if (shortenAddress.contains("강남")) {
                return Region.S01;
            } else if (shortenAddress.contains("강동")) {
                return Region.S02;
            } else if (shortenAddress.contains("강북")) {
                return Region.S03;
            } else if (shortenAddress.contains("강서")) {
                return Region.S04;
            } else if (shortenAddress.contains("관악")) {
                return Region.S05;
            } else if (shortenAddress.contains("광진")) {
                return Region.S06;
            } else if (shortenAddress.contains("구로")) {
                return Region.S07;
            } else if (shortenAddress.contains("금천")) {
                return Region.S08;
            } else if (shortenAddress.contains("노원")) {
                return Region.S09;
            } else if (shortenAddress.contains("도봉")) {
                return Region.S10;
            } else if (shortenAddress.contains("동대문")) {
                return Region.S11;
            } else if (shortenAddress.contains("동작")) {
                return Region.S12;
            } else if (shortenAddress.contains("마포")) {
                return Region.S13;
            } else if (shortenAddress.contains("서대문")) {
                return Region.S14;
            } else if (shortenAddress.contains("서초")) {
                return Region.S15;
            } else if (shortenAddress.contains("성동")) {
                return Region.S16;
            } else if (shortenAddress.contains("성북")) {
                return Region.S17;
            } else if (shortenAddress.contains("송파")) {
                return Region.S18;
            } else if (shortenAddress.contains("양천")) {
                return Region.S19;
            } else if (shortenAddress.contains("영등포")) {
                return Region.S20;
            } else if (shortenAddress.contains("용산")) {
                return Region.S21;
            } else if (shortenAddress.contains("은평")) {
                return Region.S22;
            } else if (shortenAddress.contains("종로")) {
                return Region.S23;
            } else if (shortenAddress.contains("중구")) {
                return Region.S24;
            } else {
                return Region.S25;
            }
        } else if (shortenAddress.startsWith("경기")) {
            if (shortenAddress.contains("가평")) {
                return Region.K01;
            } else if (shortenAddress.contains("고양")) {
                return Region.K02;
            } else if (shortenAddress.contains("과천")) {
                return Region.K03;
            } else if (shortenAddress.contains("광명")) {
                return Region.K04;
            } else if (shortenAddress.contains("광주")) {
                return Region.K05;
            } else if (shortenAddress.contains("구리")) {
                return Region.K06;
            } else if (shortenAddress.contains("군포")) {
                return Region.K07;
            } else if (shortenAddress.contains("김포")) {
                return Region.K08;
            } else if (shortenAddress.contains("남양주")) {
                return Region.K09;
            } else if (shortenAddress.contains("동두천")) {
                return Region.K10;
            } else if (shortenAddress.contains("부천")) {
                return Region.K11;
            } else if (shortenAddress.contains("성남")) {
                return Region.K12;
            } else if (shortenAddress.contains("수원")) {
                return Region.K13;
            } else if (shortenAddress.contains("시흥")) {
                return Region.K14;
            } else if (shortenAddress.contains("안산")) {
                return Region.K15;
            } else if (shortenAddress.contains("안성")) {
                return Region.K16;
            } else if (shortenAddress.contains("안양")) {
                return Region.K17;
            } else if (shortenAddress.contains("양주")) {
                return Region.K18;
            } else if (shortenAddress.contains("양평")) {
                return Region.K19;
            } else if (shortenAddress.contains("여주")) {
                return Region.K20;
            } else if (shortenAddress.contains("연천")) {
                return Region.K21;
            } else if (shortenAddress.contains("오산")) {
                return Region.K22;
            } else if (shortenAddress.contains("용인")) {
                return Region.K23;
            } else if (shortenAddress.contains("의왕")) {
                return Region.K24;
            } else if (shortenAddress.contains("의정부")) {
                return Region.K25;
            } else if (shortenAddress.contains("이천")) {
                return Region.K26;
            } else if (shortenAddress.contains("파주")) {
                return Region.K27;
            } else if (shortenAddress.contains("평택")) {
                return Region.K28;
            } else if (shortenAddress.contains("포천")) {
                return Region.K29;
            } else if (shortenAddress.contains("하남")) {
                return Region.K30;
            } else if (shortenAddress.contains("화성")) {
                return Region.K31;
            }
        } else if (shortenAddress.startsWith("인천")) {
            if (shortenAddress.contains("강화")) {
                return Region.I01;
            } else if (shortenAddress.contains("계양")) {
                return Region.I02;
            } else if (shortenAddress.contains("남동")) {
                return Region.I03;
            } else if (shortenAddress.contains("동구")) {
                return Region.I04;
            } else if (shortenAddress.contains("미추홀")) {
                return Region.I05;
            } else if (shortenAddress.contains("부평")) {
                return Region.I06;
            } else if (shortenAddress.contains("서구")) {
                return Region.I07;
            } else if (shortenAddress.contains("연수")) {
                return Region.I08;
            } else if (shortenAddress.contains("옹진")) {
                return Region.I09;
            } else if (shortenAddress.contains("중구")) {
                return Region.I10;
            }
        } else if (shortenAddress.startsWith("강원")) {
            if (shortenAddress.contains("강릉")) {
                return Region.KW01;
            } else if (shortenAddress.contains("고성")) {
                return Region.KW02;
            } else if (shortenAddress.contains("동해")) {
                return Region.KW03;
            } else if (shortenAddress.contains("삼척")) {
                return Region.KW04;
            } else if (shortenAddress.contains("속초")) {
                return Region.KW05;
            } else if (shortenAddress.contains("양구")) {
                return Region.KW06;
            } else if (shortenAddress.contains("양양")) {
                return Region.KW07;
            } else if (shortenAddress.contains("영월")) {
                return Region.KW08;
            } else if (shortenAddress.contains("원주")) {
                return Region.KW09;
            } else if (shortenAddress.contains("인제")) {
                return Region.KW10;
            } else if (shortenAddress.contains("정선")) {
                return Region.KW11;
            } else if (shortenAddress.contains("철원")) {
                return Region.KW12;
            } else if (shortenAddress.contains("춘천")) {
                return Region.KW13;
            } else if (shortenAddress.contains("태백")) {
                return Region.KW14;
            } else if (shortenAddress.contains("평창")) {
                return Region.KW15;
            } else if (shortenAddress.contains("홍천")) {
                return Region.KW16;
            } else if (shortenAddress.contains("화천")) {
                return Region.KW17;
            } else if (shortenAddress.contains("횡성")) {
                return Region.KW18;
            }
        } else if (shortenAddress.startsWith("충북")) {
            if (shortenAddress.contains("괴산")) {
                return Region.CB01;
            } else if (shortenAddress.contains("단양")) {
                return Region.CB02;
            } else if (shortenAddress.contains("보은")) {
                return Region.CB03;
            } else if (shortenAddress.contains("영동")) {
                return Region.CB04;
            } else if (shortenAddress.contains("옥천")) {
                return Region.CB05;
            } else if (shortenAddress.contains("음성")) {
                return Region.CB06;
            } else if (shortenAddress.contains("제천")) {
                return Region.CB07;
            } else if (shortenAddress.contains("증평")) {
                return Region.CB08;
            } else if (shortenAddress.contains("진천")) {
                return Region.CB09;
            } else if (shortenAddress.contains("청주")) {
                return Region.CB10;
            } else if (shortenAddress.contains("충주")) {
                return Region.CB11;
            }
        } else if (shortenAddress.startsWith("충남")) {
            if (shortenAddress.contains("계룡")) {
                return Region.CN01;
            } else if (shortenAddress.contains("공주")) {
                return Region.CN02;
            } else if (shortenAddress.contains("금산")) {
                return Region.CN03;
            } else if (shortenAddress.contains("논산")) {
                return Region.CN04;
            } else if (shortenAddress.contains("당진")) {
                return Region.CN05;
            } else if (shortenAddress.contains("보령")) {
                return Region.CN06;
            } else if (shortenAddress.contains("부여")) {
                return Region.CN07;
            } else if (shortenAddress.contains("서산")) {
                return Region.CN08;
            } else if (shortenAddress.contains("서천")) {
                return Region.CN09;
            } else if (shortenAddress.contains("아산")) {
                return Region.CN10;
            } else if (shortenAddress.contains("예산")) {
                return Region.CN11;
            } else if (shortenAddress.contains("천안")) {
                return Region.CN12;
            } else if (shortenAddress.contains("청양")) {
                return Region.CN13;
            } else if (shortenAddress.contains("태안")) {
                return Region.CN14;
            } else if (shortenAddress.contains("홍성")) {
                return Region.CN15;
            }
        } else if (shortenAddress.startsWith("대전")) {
            if (shortenAddress.contains("대덕")) {
                return Region.DJ01;
            } else if (shortenAddress.contains("동구")) {
                return Region.DJ02;
            } else if (shortenAddress.contains("서구")) {
                return Region.DJ03;
            } else if (shortenAddress.contains("유성")) {
                return Region.DJ04;
            } else if (shortenAddress.contains("중구")) {
                return Region.DJ05;
            }
        } else if (shortenAddress.startsWith("세종")) {
            return Region.SGG;
        } else if (shortenAddress.startsWith("경북")) {
            if (shortenAddress.contains("경산")) {
                return Region.GB01;
            } else if (shortenAddress.contains("경주")) {
                return Region.GB02;
            } else if (shortenAddress.contains("고령")) {
                return Region.GB03;
            } else if (shortenAddress.contains("구미")) {
                return Region.GB04;
            } else if (shortenAddress.contains("군위")) {
                return Region.GB05;
            } else if (shortenAddress.contains("김천")) {
                return Region.GB06;
            } else if (shortenAddress.contains("문경")) {
                return Region.GB07;
            } else if (shortenAddress.contains("봉화")) {
                return Region.GB08;
            } else if (shortenAddress.contains("상주")) {
                return Region.GB09;
            } else if (shortenAddress.contains("성주")) {
                return Region.GB10;
            } else if (shortenAddress.contains("안동")) {
                return Region.GB11;
            } else if (shortenAddress.contains("영덕")) {
                return Region.GB12;
            } else if (shortenAddress.contains("영양")) {
                return Region.GB13;
            } else if (shortenAddress.contains("영주")) {
                return Region.GB14;
            } else if (shortenAddress.contains("영천")) {
                return Region.GB15;
            } else if (shortenAddress.contains("예천")) {
                return Region.GB16;
            } else if (shortenAddress.contains("울릉")) {
                return Region.GB17;
            } else if (shortenAddress.contains("울진")) {
                return Region.GB18;
            } else if (shortenAddress.contains("의성")) {
                return Region.GB19;
            } else if (shortenAddress.contains("청도")) {
                return Region.GB20;
            } else if (shortenAddress.contains("청송")) {
                return Region.GB21;
            } else if (shortenAddress.contains("칠곡")) {
                return Region.GB22;
            } else if (shortenAddress.contains("포항")) {
                return Region.GB23;
            }
        } else if (shortenAddress.startsWith("경남")) {
            if (shortenAddress.contains("거제")) {
                return Region.GN01;
            } else if (shortenAddress.contains("거창")) {
                return Region.GN02;
            } else if (shortenAddress.contains("고성")) {
                return Region.GN03;
            } else if (shortenAddress.contains("김해")) {
                return Region.GN04;
            } else if (shortenAddress.contains("남해")) {
                return Region.GN05;
            } else if (shortenAddress.contains("밀양")) {
                return Region.GN06;
            } else if (shortenAddress.contains("사천")) {
                return Region.GN07;
            } else if (shortenAddress.contains("산청")) {
                return Region.GN08;
            } else if (shortenAddress.contains("양산")) {
                return Region.GN09;
            } else if (shortenAddress.contains("의령")) {
                return Region.GN10;
            } else if (shortenAddress.contains("진주")) {
                return Region.GN11;
            } else if (shortenAddress.contains("창녕")) {
                return Region.GN12;
            } else if (shortenAddress.contains("창원")) {
                return Region.GN13;
            } else if (shortenAddress.contains("통영")) {
                return Region.GN14;
            } else if (shortenAddress.contains("하동")) {
                return Region.GN15;
            } else if (shortenAddress.contains("함안")) {
                return Region.GN16;
            } else if (shortenAddress.contains("함양")) {
                return Region.GN17;
            } else if (shortenAddress.contains("합천")) {
                return Region.GN18;
            }
        } else if (shortenAddress.startsWith("대구")) {
            if (shortenAddress.contains("남구")) {
                return Region.D01;
            } else if (shortenAddress.contains("달서")) {
                return Region.D02;
            } else if (shortenAddress.contains("달성")) {
                return Region.D03;
            } else if (shortenAddress.contains("동구")) {
                return Region.D04;
            } else if (shortenAddress.contains("북구")) {
                return Region.D05;
            } else if (shortenAddress.contains("서구")) {
                return Region.D06;
            } else if (shortenAddress.contains("수성")) {
                return Region.D07;
            } else if (shortenAddress.contains("중구")) {
                return Region.D08;
            }
        } else if (shortenAddress.startsWith("울산")) {
            if (shortenAddress.contains("남구")) {
                return Region.U01;
            } else if (shortenAddress.contains("동구")) {
                return Region.U02;
            } else if (shortenAddress.contains("북구")) {
                return Region.U03;
            } else if (shortenAddress.contains("울주")) {
                return Region.U04;
            } else if (shortenAddress.contains("중구")) {
                return Region.U05;
            }
        } else if (shortenAddress.startsWith("부산")) {
            if (shortenAddress.contains("강서")) {
                return Region.B01;
            } else if (shortenAddress.contains("금정")) {
                return Region.B02;
            } else if (shortenAddress.contains("기장")) {
                return Region.B03;
            } else if (shortenAddress.contains("남구")) {
                return Region.B04;
            } else if (shortenAddress.contains("동구")) {
                return Region.B05;
            } else if (shortenAddress.contains("동래")) {
                return Region.B06;
            } else if (shortenAddress.contains("부산진")) {
                return Region.B07;
            } else if (shortenAddress.contains("북구")) {
                return Region.B08;
            } else if (shortenAddress.contains("사상")) {
                return Region.B09;
            } else if (shortenAddress.contains("사하")) {
                return Region.B10;
            } else if (shortenAddress.contains("서구")) {
                return Region.B11;
            } else if (shortenAddress.contains("수영")) {
                return Region.B12;
            } else if (shortenAddress.contains("연제")) {
                return Region.B13;
            } else if (shortenAddress.contains("영도")) {
                return Region.B14;
            } else if (shortenAddress.contains("중구")) {
                return Region.B15;
            } else if (shortenAddress.contains("해운대")) {
                return Region.B16;
            }
        } else if (shortenAddress.startsWith("전북")) {
            if (shortenAddress.contains("고창")) {
                return Region.JB01;
            } else if (shortenAddress.contains("군산")) {
                return Region.JB02;
            } else if (shortenAddress.contains("김제")) {
                return Region.JB03;
            } else if (shortenAddress.contains("남원")) {
                return Region.JB04;
            } else if (shortenAddress.contains("무주")) {
                return Region.JB05;
            } else if (shortenAddress.contains("부안")) {
                return Region.JB06;
            } else if (shortenAddress.contains("순창")) {
                return Region.JB07;
            } else if (shortenAddress.contains("완주")) {
                return Region.JB08;
            } else if (shortenAddress.contains("익산")) {
                return Region.JB09;
            } else if (shortenAddress.contains("임실")) {
                return Region.JB10;
            } else if (shortenAddress.contains("장수")) {
                return Region.JB11;
            } else if (shortenAddress.contains("전주")) {
                return Region.JB12;
            } else if (shortenAddress.contains("정읍")) {
                return Region.JB13;
            } else if (shortenAddress.contains("진안")) {
                return Region.JB14;
            }
        } else if (shortenAddress.startsWith("전남")) {
            if (shortenAddress.contains("강진")) {
                return Region.JN01;
            } else if (shortenAddress.contains("고흥")) {
                return Region.JN02;
            } else if (shortenAddress.contains("곡성")) {
                return Region.JN03;
            } else if (shortenAddress.contains("광양")) {
                return Region.JN04;
            } else if (shortenAddress.contains("구례")) {
                return Region.JN05;
            } else if (shortenAddress.contains("나주")) {
                return Region.JN06;
            } else if (shortenAddress.contains("담양")) {
                return Region.JN07;
            } else if (shortenAddress.contains("목포")) {
                return Region.JN08;
            } else if (shortenAddress.contains("무안")) {
                return Region.JN09;
            } else if (shortenAddress.contains("보성")) {
                return Region.JN10;
            } else if (shortenAddress.contains("순천")) {
                return Region.JN11;
            } else if (shortenAddress.contains("신안")) {
                return Region.JN12;
            } else if (shortenAddress.contains("여수")) {
                return Region.JN13;
            } else if (shortenAddress.contains("영광")) {
                return Region.JN14;
            } else if (shortenAddress.contains("영암")) {
                return Region.JN15;
            } else if (shortenAddress.contains("완도")) {
                return Region.JN16;
            } else if (shortenAddress.contains("장성")) {
                return Region.JN17;
            } else if (shortenAddress.contains("장흥")) {
                return Region.JN18;
            } else if (shortenAddress.contains("진도")) {
                return Region.JN19;
            } else if (shortenAddress.contains("함평")) {
                return Region.JN20;
            } else if (shortenAddress.contains("해남")) {
                return Region.JN21;
            } else if (shortenAddress.contains("화순")) {
                return Region.JN22;
            }
        } else if (shortenAddress.startsWith("광주")) {
            if (shortenAddress.contains("광산")) {
                return Region.G01;
            } else if (shortenAddress.contains("남구")) {
                return Region.G02;
            } else if (shortenAddress.contains("동구")) {
                return Region.G03;
            } else if (shortenAddress.contains("북구")) {
                return Region.G04;
            } else if (shortenAddress.contains("서구")) {
                return Region.G05;
            }
        } else if (shortenAddress.startsWith("제주")) {
            if (shortenAddress.contains("서귀포")) {
                return Region.JJ01;
            } else if (shortenAddress.contains("제주")) {
                return Region.JJ02;
            }
        }

        return region;
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
