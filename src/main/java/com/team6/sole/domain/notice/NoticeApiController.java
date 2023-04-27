package com.team6.sole.domain.notice;

import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.notice.dto.NoticeRequestDto;
import com.team6.sole.domain.notice.dto.NoticeResponseDto;
import com.team6.sole.global.config.CommonApiResponse;
import com.team6.sole.global.config.security.jwt.annotation.LoginUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Api(tags = "공지사항")
@RequestMapping("api/notices")
public class NoticeApiController {
    private final NoticeService noticeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @ApiOperation(value = "공지사항 등록")
    public ResponseEntity<CommonApiResponse<NoticeResponseDto>> makeNotice(
            @ApiIgnore @LoginUser Member member,
            @RequestBody NoticeRequestDto noticeRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(noticeService.makeNotice(member, noticeRequestDto)));
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

    @PutMapping("{noticeId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @ApiOperation(value = "공지사항 수정")
    public ResponseEntity<CommonApiResponse<NoticeResponseDto>> modNotice(
            @PathVariable Long noticeId,
            @RequestBody NoticeRequestDto noticeRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(noticeService.modNotice(noticeId, noticeRequestDto)));
    }

    @GetMapping("test")
    public ResponseEntity<CommonApiResponse<String>> test(
            @ApiIgnore @LoginUser Member member) {
        return ResponseEntity.ok(CommonApiResponse.of(noticeService.test(member)));
    }
}
