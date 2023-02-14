package com.team6.sole.domain.member;

import com.amazonaws.Response;
import com.team6.sole.domain.member.dto.DuplicateNickname;
import com.team6.sole.domain.member.dto.MemberRequestDto;
import com.team6.sole.domain.member.dto.MemberResponseDto;
import com.team6.sole.domain.member.dto.OauthRequest;
import com.team6.sole.global.config.CommonApiResponse;
import com.team6.sole.global.config.security.dto.TokenResponseDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Api(tags = "소셜 로그인")
@RequestMapping("api/members")
public class MemberApiController {
    private final MemberService memberService;

    @PostMapping(value = "{provider}")
    @ApiOperation(value = "소셜 로그인(카카오 및 애플)")
    public ResponseEntity<CommonApiResponse<MemberResponseDto>> makeMember(
            @PathVariable String provider,
            @RequestPart OauthRequest oauthRequest,
            @RequestPart(required = false) MemberRequestDto memberRequestDto,
            @RequestPart(required = false) MultipartFile multipartFile) {
        return memberService.makeMember(provider, oauthRequest, memberRequestDto, multipartFile);
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
}
