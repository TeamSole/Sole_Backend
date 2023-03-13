package com.team6.sole.domain.history;

import com.team6.sole.domain.history.dto.HistoryResponseDto;
import com.team6.sole.domain.history.dto.HistorySearchRequestDto;
import com.team6.sole.domain.home.dto.HomeResponseDto;
import com.team6.sole.global.config.CommonApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Api(tags = "나의 기록")
@RequestMapping("api/histories")
public class HistoryApiController {
    private final HistoryService historyService;

    @GetMapping
    @ApiOperation(value = "나의 기록 조회(상단)")
    public ResponseEntity<CommonApiResponse<HistoryResponseDto>> showMyHistory(
            @ApiIgnore Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(historyService.showMyHistory(authentication.getName())));
    }
    
    @PostMapping("courses")
    @ApiOperation(value = "나의 기록 조회(하단)")
    public ResponseEntity<CommonApiResponse<List<HomeResponseDto>>> showMyCourseHistories(
            @ApiIgnore Authentication authentication,
            @RequestParam(required = false) Long courseId,
            @RequestBody(required = false) HistorySearchRequestDto historySearchRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(historyService.showMyCourseHistories(authentication.getName(), courseId, historySearchRequestDto)));
    }
}
