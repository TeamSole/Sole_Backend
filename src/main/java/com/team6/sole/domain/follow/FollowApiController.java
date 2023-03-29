package com.team6.sole.domain.follow;

import com.team6.sole.domain.follow.dto.FollowDetailResponseDto;
import com.team6.sole.domain.follow.dto.FollowInfoResponseDto;
import com.team6.sole.domain.follow.dto.FollowResponseDto;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.global.config.CommonApiResponse;
import com.team6.sole.global.config.security.jwt.annotation.LoginUser;
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
            @ApiIgnore @LoginUser Member member) {
        return ResponseEntity.ok(CommonApiResponse.of(followService.showFollowingCourses(member)));
    }

    @GetMapping("followings")
    @ApiOperation(value = "팔로잉 보기")
    public ResponseEntity<CommonApiResponse<List<FollowResponseDto>>> showFollowings(
            @ApiIgnore @LoginUser Member member) {
        return ResponseEntity.ok(CommonApiResponse.of(followService.showFollowings(member)));
    }

    @GetMapping("followers")
    @ApiOperation(value = "팔로워 보기")
    public ResponseEntity<CommonApiResponse<List<FollowResponseDto>>> showFollowers(
            @ApiIgnore @LoginUser Member member) {
        return ResponseEntity.ok(CommonApiResponse.of(followService.showFollowers(member)));
    }

    @GetMapping("{followInfoMemberSocialId}")
    @ApiOperation(value = "팔로우 상대 상세정보 확인")
    public ResponseEntity<CommonApiResponse<FollowInfoResponseDto>> showFollowInfo(
            @ApiIgnore @LoginUser Member member,
            @PathVariable String followInfoMemberSocialId,
            @RequestParam(required = false) Long courseId) {
        return ResponseEntity.ok(CommonApiResponse.of(followService.showFollowInfo(member, followInfoMemberSocialId, courseId)));
    }

    @PostMapping("follow/{toMemberId}")
    @ApiOperation(value = "팔로우 및 언팔로우")
    public ResponseEntity<CommonApiResponse<String>> toFollow(
            @ApiIgnore @LoginUser Member fromMember,
            @PathVariable Long toMemberId) {
        return ResponseEntity.ok(CommonApiResponse.of(followService.toFollow(fromMember, toMemberId)));
    }
}
