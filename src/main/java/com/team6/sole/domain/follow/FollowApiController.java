package com.team6.sole.domain.follow;

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

    @PostMapping("follow/{toMemberId}")
    @ApiOperation(value = "팔로우")
    public ResponseEntity<CommonApiResponse<String>> toFollow(
            @ApiIgnore Authentication authentication,
            @PathVariable Long toMemberId) {
        return ResponseEntity.ok(CommonApiResponse.of(followService.toFollow(authentication.getName(), toMemberId)));
    }

    @DeleteMapping("unfollow/{followId}")
    @ApiOperation(value = "언팔로우")
    public ResponseEntity<CommonApiResponse<String>> unFollow(
            @PathVariable Long followId) {
        return ResponseEntity.ok(CommonApiResponse.of(followService.unFollow(followId)));
    }
}
