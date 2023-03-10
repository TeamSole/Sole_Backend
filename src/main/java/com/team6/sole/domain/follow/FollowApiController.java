package com.team6.sole.domain.follow;

import com.team6.sole.domain.follow.dto.FollowDetailResponseDto;
import com.team6.sole.domain.follow.dto.FollowInfoResponseDto;
import com.team6.sole.domain.follow.dto.FollowResponseDto;
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
@Api(tags = "팔로우")
@RequestMapping("api/follows")
public class FollowApiController {
    private final FollowService followService;

    @GetMapping
    @ApiOperation(value = "팔로잉 하는 사람들 코스 보기")
    public ResponseEntity<CommonApiResponse<List<FollowDetailResponseDto>>> showFollowingsCourses(
            @ApiIgnore Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(followService.showFollowingCourses(authentication.getName())));
    }

    @GetMapping("followings")
    @ApiOperation(value = "팔로잉 보기")
    public ResponseEntity<CommonApiResponse<List<FollowResponseDto>>> showFollowings(
            @ApiIgnore Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(followService.showFollowings(authentication.getName())));
    }

    @GetMapping("followers")
    @ApiOperation(value = "팔로워 보기")
    public ResponseEntity<CommonApiResponse<List<FollowResponseDto>>> showFollowers(
            @ApiIgnore Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(followService.showFollowers(authentication.getName())));
    }

    @GetMapping("{followInfoMemberSocialId}")
    @ApiOperation(value = "팔로우 상대 상세정보 확인")
    public ResponseEntity<CommonApiResponse<FollowInfoResponseDto>> showFollowInfo(
            @ApiIgnore Authentication authentication,
            @PathVariable String followInfoMemberSocialId,
            @RequestParam(required = false) Long courseId) {
        return ResponseEntity.ok(CommonApiResponse.of(followService.showFollowInfo(authentication.getName(), followInfoMemberSocialId, courseId)));
    }

    @PostMapping("follow/{toMemberId}")
    @ApiOperation(value = "팔로우 및 언팔로우")
    public ResponseEntity<CommonApiResponse<String>> toFollow(
            @ApiIgnore Authentication authentication,
            @PathVariable Long toMemberId) {
        return ResponseEntity.ok(CommonApiResponse.of(followService.toFollow(authentication.getName(), toMemberId)));
    }
}
