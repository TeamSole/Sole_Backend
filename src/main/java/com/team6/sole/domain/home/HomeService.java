package com.team6.sole.domain.home;

import com.team6.sole.domain.home.dto.CourseRequestDto;
import com.team6.sole.domain.home.dto.CourseResponseDto;
import com.team6.sole.domain.home.dto.PlaceRequestDto;
import com.team6.sole.domain.home.entity.Category;
import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.Gps;
import com.team6.sole.domain.home.entity.Place;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class HomeService {
    private final CourseRepository courseRepository;
    private final PlaceRepository placeRepository;
    private final MemberRepository memberRepository;
    private final AwsS3ServiceImpl awsS3Service;
    
    // 코스 등록
    @SneakyThrows
    @Transactional
    public CourseResponseDto makeCourse(String socialId, CourseRequestDto courseRequestDto, MultipartFile thumnailImg, Map<String, List<MultipartFile>> courseImagesMap) {
        Member writer = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        // 코스 저장
        Course course = Course.builder()
                .thumbnailUrl(thumnailImg == null
                        ? null
                        : awsS3Service.uploadImage(thumnailImg, "course"))
                .scrapCount(0)
                .title(courseRequestDto.getTitle())
                .description(courseRequestDto.getDescription())
                .startDate(formatter.parse(courseRequestDto.getDate()))
                .duration(courseRequestDto.getDuration())
                .distance(courseRequestDto.getDistance())
                .category(
                        Category.builder()
                                .placeCategories(courseRequestDto.getPlaceCategories())
                                .withCategories(courseRequestDto.getWithCategories())
                                .transCategories(courseRequestDto.getTransCategories())
                                .build()
                )
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
                    .placeImgUrls(
                            courseImagesMap.get(placeRequestDto.getPlaceName()) == null
                                    ? null
                                    : awsS3Service.uploadImage(courseImagesMap.get(placeRequestDto.getPlaceName()), "place")
                    )
                    .course(course)
                    .build();
            placeRepository.save(place);
            course.putPlace(place);
        }

        return CourseResponseDto.of(course);
    }
}
