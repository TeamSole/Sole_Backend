package com.team6.sole.domain.mypage;

import com.team6.sole.domain.mypage.dto.*;
import com.team6.sole.global.config.CommonApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Api(tags = "마이페이지")
@RequestMapping("api/mypage")
public class MypageApiController {
    private final MypageService mypageService;

    @GetMapping
    @ApiOperation(value = "마이페이지 조회")
    public ResponseEntity<CommonApiResponse<MypageResponseDto>> showMypage(
            @ApiIgnore Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(mypageService.showMypage(authentication.getName())));
    }

    @PutMapping
    @ApiOperation(value = "마이페이지 수정")
    public ResponseEntity<CommonApiResponse<MypageResponseDto>> modMypage(
            @ApiIgnore Authentication authentication,
            @RequestPart(required = false) MultipartFile multipartFile,
            @RequestPart MypageRequestDto mypageRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(mypageService.modMypage(authentication.getName(), multipartFile, mypageRequestDto)));
    }

    @GetMapping("notification")
    @ApiOperation(value = "알림 설정 조회")
    public ResponseEntity<CommonApiResponse<NotSettingReseponseDto>> showNotSetting(
            @ApiIgnore Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(mypageService.showNotSetting(authentication.getName())));
    }

    @PutMapping("notification")
    @ApiOperation(value = "알림 설정")
    public ResponseEntity<CommonApiResponse<NotSettingReseponseDto>> modNotSetting(
            @ApiIgnore Authentication authentication,
            @RequestBody NotSettingRequestDto notSettingRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(mypageService.modNotSetting(authentication.getName(), notSettingRequestDto)));
    }

    @GetMapping("notification/histories")
    @ApiOperation(value = "알림 내역 조회")
    public ResponseEntity<CommonApiResponse<List<NotHistoryResponseDto>>> showNotHistories(
            @ApiIgnore Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(mypageService.showNotHistories(authentication.getName())));
    }

    @DeleteMapping("quit")
    @ApiOperation(value = "회원 탈퇴")
    public ResponseEntity<CommonApiResponse<String>> delMember(
            @ApiIgnore Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(mypageService.delMember(authentication.getName())));
    }
}
