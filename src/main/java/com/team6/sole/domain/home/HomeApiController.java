package com.team6.sole.domain.home;

import com.team6.sole.domain.home.dto.CourseRequestDto;
import com.team6.sole.domain.home.dto.CourseResponseDto;
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

    @PostMapping
    @ApiOperation(value = "코스 등록")
    public ResponseEntity<CommonApiResponse<CourseResponseDto>> makeCourse(
            @ApiIgnore Authentication authentication,
            @RequestPart(required = false) MultipartFile thumnailImg,
            @RequestPart CourseRequestDto courseRequestDto,
            HttpServletRequest request) {
        Map<String, List<MultipartFile>> courseImagesMap = new HashMap<>();
        if (request instanceof MultipartHttpServletRequest) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            courseImagesMap = multiRequest.getMultiFileMap();
        }
        return ResponseEntity.ok(CommonApiResponse.of(homeService.makeCourse(authentication.getName(), courseRequestDto, thumnailImg, courseImagesMap)));
    }
}
