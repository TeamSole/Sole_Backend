package com.team6.sole.domain.home;

import com.team6.sole.domain.home.dto.*;
import com.team6.sole.global.config.CommonApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Api(tags = "홈")
@RequestMapping("api/courses")
public class HomeApiController {
    private final HomeService homeService;

    @PatchMapping("currentGps")
    @ApiOperation(value = "현재 위치 변경")
    public ResponseEntity<CommonApiResponse<GpsResponseDto>> setCurrentGps(
            @ApiIgnore Authentication authentication,
            @RequestBody GpsReqeustDto gpsRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(homeService.setCurrentGps(authentication.getName(), gpsRequestDto)));
    }
    
    @GetMapping
    @ApiOperation(value = "홈 보기")
    public ResponseEntity<CommonApiResponse<List<HomeResponseDto>>> showHomes(
            @ApiIgnore Authentication authentication,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String searchWord) {
        if (searchWord != null) {
            return ResponseEntity.ok(CommonApiResponse.of(homeService.searchHomes(authentication.getName(), courseId, searchWord)));
        }
        return ResponseEntity.ok(CommonApiResponse.of(homeService.showHomes(authentication.getName(), courseId)));
    }

    @PostMapping
    @ApiOperation(value = "코스 등록")
    public ResponseEntity<CommonApiResponse<CourseResponseDto>> makeCourse(
            @ApiIgnore Authentication authentication,
            @RequestPart CourseRequestDto courseRequestDto,
            MultipartHttpServletRequest multipartHttpServletRequest) {
        Map<String, List<MultipartFile>> courseImagesMap = multipartHttpServletRequest.getMultiFileMap();
        return ResponseEntity.ok(CommonApiResponse.of(homeService.makeCourse(authentication.getName(), courseRequestDto, courseImagesMap)));
    }

    @GetMapping("{courseId}")
    @ApiOperation(value = "코스 상세보기")
    public ResponseEntity<CommonApiResponse<CourseDetailResponseDto>> showCourseDetail(
            @ApiIgnore Authentication authentication,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(CommonApiResponse.of(homeService.showCourseDetail(courseId, authentication.getName())));
    }

    @PutMapping("{courseId}")
    @ApiOperation(value = "코스 수정")
    public ResponseEntity<CommonApiResponse<CourseResponseDto>> modCourse(
            @PathVariable Long courseId,
            MultipartHttpServletRequest multipartHttpServletRequest,
            @RequestPart CourseUpdateRequestDto courseUpdateRequestDto) {
        Map<String, List<MultipartFile>> placeImagesMap = multipartHttpServletRequest.getMultiFileMap();
        return ResponseEntity.ok(CommonApiResponse.of(homeService.modCourse(courseId, placeImagesMap, courseUpdateRequestDto)));
    }

    @DeleteMapping("{courseId}")
    @ApiOperation(value = "코스 삭제")
    public ResponseEntity<Void> delCourse(
            @PathVariable Long courseId) {
        homeService.delCourse(courseId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("recommend")
    @ApiOperation(value = "추천 코스 보기")
    public ResponseEntity<CommonApiResponse<List<RecommendCourseResponseDto>>> showRecommendCourse(
            @ApiIgnore Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(homeService.showRecommendCourses(authentication.getName())));
    }

    @PostMapping("{courseId}/scrap")
    @ApiOperation(value = "코스 스크랩 및 취소")
    public ResponseEntity<Void> scrapCourse(
            @ApiIgnore Authentication authentication,
            @PathVariable Long courseId) {
        homeService.scrapCourse(authentication.getName(), courseId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("favCategory")
    @ApiOperation(value = "선호 카테고리 보기")
    public ResponseEntity<CommonApiResponse<FavCategoryResponseDto>> showFavCategory(
            @ApiIgnore Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(homeService.showFavCategory(authentication.getName())));
    }

    @PatchMapping("favCategory")
    @ApiOperation(value = "선호 카테고리 수정")
    public ResponseEntity<CommonApiResponse<FavCategoryResponseDto>> modFavCategory(
            @ApiIgnore Authentication authentication,
            @RequestBody FavCategoryRequestDto favCategoryRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(homeService.modFavCategory(authentication.getName(), favCategoryRequestDto)));
    }
}
