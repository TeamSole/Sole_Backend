package com.team6.sole.domain.home;

import com.team6.sole.domain.home.dto.*;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.global.config.CommonApiResponse;
import com.team6.sole.global.config.security.jwt.annotation.LoginUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Api(tags = "홈")
@RequestMapping("api/courses")
public class HomeApiController {
    private final HomeService homeService;

    @GetMapping("currentGps")
    @ApiOperation(value = "현재 위치 주소 조회")
    public ResponseEntity<CommonApiResponse<String>> getCurrentGps(
            @ApiIgnore @LoginUser Member member) {
        return ResponseEntity.ok(CommonApiResponse.of(homeService.showCurrentGps(member)));
    }

    @PatchMapping("currentGps")
    @ApiOperation(value = "현재 위치 변경")
    public ResponseEntity<CommonApiResponse<GpsResponseDto>> setCurrentGps(
            @ApiIgnore @LoginUser Member member,
            @RequestBody GpsReqeustDto gpsRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(homeService.setCurrentGps(member, gpsRequestDto)));
    }
    
    @GetMapping
    @ApiOperation(value = "홈 보기")
    public ResponseEntity<CommonApiResponse<List<HomeResponseDto>>> showHomes(
            @ApiIgnore @LoginUser Member member,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String searchWord) {
        if (searchWord != null) {
            return ResponseEntity.ok(CommonApiResponse.of(homeService.searchHomes(member, courseId, searchWord)));
        }
        return ResponseEntity.ok(CommonApiResponse.of(homeService.showHomes(member, courseId)));
    }

    @PostMapping
    @ApiOperation(value = "코스 등록")
    public ResponseEntity<CommonApiResponse<CourseResponseDto>> makeCourse(
            @ApiIgnore @LoginUser Member member,
            @RequestPart CourseRequestDto courseRequestDto,
            MultipartHttpServletRequest multipartHttpServletRequest) {
        Map<String, List<MultipartFile>> courseImagesMap = multipartHttpServletRequest.getMultiFileMap();
        return ResponseEntity.ok(CommonApiResponse.of(homeService.makeCourse(member, courseRequestDto, courseImagesMap)));
    }

    @GetMapping("{courseId}")
    @ApiOperation(value = "코스 상세보기")
    public ResponseEntity<CommonApiResponse<CourseDetailResponseDto>> showCourseDetail(
            @ApiIgnore @LoginUser Member member,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(CommonApiResponse.of(homeService.showCourseDetail(courseId, member)));
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
    public ResponseEntity<CommonApiResponse<Boolean>> delCourse(
            @PathVariable Long courseId) {
        homeService.delCourse(courseId);
        return ResponseEntity.ok(CommonApiResponse.of(true));
    }
    
    @GetMapping("recommendTest")
    @ApiOperation(value = "추천 코스 보기테스트")
    public ResponseEntity<CommonApiResponse<List<RecommendCourseResponseDto>>> showRecommendTest(
            @ApiIgnore @LoginUser Member member) {
        return ResponseEntity.ok(CommonApiResponse.of(homeService.showRecommendTest(member)));
    }

    @GetMapping("recommend")
    @ApiOperation(value = "추천 코스 보기")
    public ResponseEntity<CommonApiResponse<List<RecommendCourseResponseDto>>> showRecommendCourse(
            @ApiIgnore @LoginUser Member member) {
        return ResponseEntity.ok(CommonApiResponse.of(homeService.showRecommendCourses(member)));
    }

    @PostMapping("{courseId}")
    @ApiOperation(value = "코스 스크랩 및 취소")
    public ResponseEntity<Void> scrapCourse(
            @ApiIgnore @LoginUser Member member,
            @PathVariable Long courseId,
            @RequestParam(required = false) Long scrapFolderId) {
        homeService.scrapCourse(member, courseId, scrapFolderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("{courseId}/declare")
    @ApiOperation(value = "코스 신고")
    public ResponseEntity<CommonApiResponse<String>> declareCourse(
            @ApiIgnore @LoginUser Member member,
            @PathVariable Long courseId) {
        return ResponseEntity.ok(CommonApiResponse.of(homeService.declareCourse(member, courseId)));
    }

    @GetMapping("favCategory")
    @ApiOperation(value = "선호 카테고리 보기")
    public ResponseEntity<CommonApiResponse<FavCategoryResponseDto>> showFavCategory(
            @ApiIgnore @LoginUser Member member) {
        return ResponseEntity.ok(CommonApiResponse.of(homeService.showFavCategory(member)));
    }

    @PatchMapping("favCategory")
    @ApiOperation(value = "선호 카테고리 수정")
    public ResponseEntity<CommonApiResponse<FavCategoryResponseDto>> modFavCategory(
            @ApiIgnore @LoginUser Member member,
            @RequestBody FavCategoryRequestDto favCategoryRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(homeService.modFavCategory(member, favCategoryRequestDto)));
    }

    @PostMapping("/imageTest")
    @ApiOperation(value = "이미지 테스트")
    public ResponseEntity<String> upload(MultipartHttpServletRequest multipartHttpServletRequest) {
        Map<String, List<MultipartFile>> fileMap = multipartHttpServletRequest.getMultiFileMap(); // 파일들을 Map 자료구조에 담아 가져오기

        return ResponseEntity.ok(homeService.imageTest(fileMap));
    }
}
