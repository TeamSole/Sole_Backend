package com.team6.sole.domain.member;

import com.team6.sole.domain.member.dto.*;
import com.team6.sole.global.config.CommonApiResponse;
import com.team6.sole.global.config.security.dto.TokenResponseDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequiredArgsConstructor
@Api(tags = "소셜 로그인")
@RequestMapping("api/members")
public class MemberApiController {
    private final MemberService memberService;

    @PostMapping("{provider}")
    @ApiOperation(value = "회원체크 및 로그인(소셜)")
    public ResponseEntity<CommonApiResponse<MemberResponseDto>> checkMember(
            @PathVariable String provider,
            @RequestBody OauthRequest oauthRequest) {
        return memberService.checkMember(provider, oauthRequest);
    }

    @PostMapping("{provider}/signup")
    @ApiOperation(value = "회원가입(소셜)")
    public ResponseEntity<CommonApiResponse<MemberResponseDto>> makeMember(
            @PathVariable String provider,
            @RequestPart(required = false) MultipartFile multipartFile,
            @RequestPart MemberRequestDto memberRequestDto) {
        return memberService.makeMember(provider, multipartFile, memberRequestDto);
    }
    
    @PatchMapping("fcmToken")
    @ApiOperation(value = "fcmToken 교체")
    public ResponseEntity<CommonApiResponse<String>> modFcmToken(
            @ApiIgnore Authentication authentication,
            @RequestBody FcmTokenDto fcmTokenDto) {
        return ResponseEntity.ok(CommonApiResponse.of(memberService.modFcmToken(authentication.getName(), fcmTokenDto)));
    }

    @PatchMapping("logout")
    @ApiOperation(value = "로그아웃(fcmToken 삭제)")
    public ResponseEntity<CommonApiResponse<String>> logout(
            @ApiIgnore Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(memberService.logout(authentication.getName())));
    }

    @PostMapping("reissue")
    @ApiOperation(value = "토큰 재발급")
    public ResponseEntity<CommonApiResponse<TokenResponseDto>> reissue(
            @RequestHeader("Authorization") String accessToken,
            @RequestHeader("Refresh") String refreshToken) {
        return memberService.reissue(accessToken, refreshToken);
    }

    @PostMapping("nickname")
    @ApiOperation(value = "닉네임 중복 체크")
    public ResponseEntity<Boolean> duplicateNickname(@RequestBody DuplicateNickname duplicateNickname) {
        return ResponseEntity.ok(memberService.duplicateNickname(duplicateNickname.getNickname()));
    }

    @PostMapping("test")
    @ApiIgnore
    public ResponseEntity<String> test(@RequestPart MemberRequestDto memberRequestDto) {
        return ResponseEntity.ok(memberRequestDto.getNickname());
    }

    @PostMapping("testLogin")
    public ResponseEntity<CommonApiResponse<MemberResponseDto>> checkMember() {
        return memberService.checkMember();
    }
}
