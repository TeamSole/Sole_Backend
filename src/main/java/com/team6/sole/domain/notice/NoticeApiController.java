package com.team6.sole.domain.notice;

import com.team6.sole.domain.notice.dto.NoticeRequestDto;
import com.team6.sole.domain.notice.dto.NoticeResponseDto;
import com.team6.sole.global.config.CommonApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Api(tags = "공지사항")
@RequestMapping("api/notices")
public class NoticeApiController {
    private NoticeService noticeService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "공지사항 등록")
    public ResponseEntity<CommonApiResponse<NoticeResponseDto>> makeNotice(
            @ApiIgnore Authentication authentication,
            @RequestBody NoticeRequestDto noticeRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(noticeService.makeNotice(authentication.getName(), noticeRequestDto)));
    }

    @GetMapping
    @ApiOperation(value = "공지사항 조회")
    public ResponseEntity<CommonApiResponse<List<NoticeResponseDto>>> showNotices() {
        return ResponseEntity.ok(CommonApiResponse.of(noticeService.showNotices()));
    }

    @GetMapping("{noticeId}")
    @ApiOperation(value = "공지사항 상세 조회")
    public ResponseEntity<CommonApiResponse<NoticeResponseDto>> showNotice(
            @PathVariable Long noticeId) {
        return ResponseEntity.ok(CommonApiResponse.of(noticeService.showNotice(noticeId)));
    }
}
