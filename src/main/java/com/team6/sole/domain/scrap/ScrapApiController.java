package com.team6.sole.domain.scrap;

import com.team6.sole.domain.home.dto.HomeResponseDto;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.scrap.dto.NewScrapFolderRequestDto;
import com.team6.sole.domain.scrap.dto.NewScrapFolderResponseDto;
import com.team6.sole.domain.scrap.dto.ScrapFolderResponseDto;
import com.team6.sole.domain.scrap.dto.ScrapFolderRequestDto;
import com.team6.sole.global.config.CommonApiResponse;
import com.team6.sole.global.config.security.jwt.annotation.LoginUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Api(tags = "스크랩")
@RequestMapping("api/scraps")
public class ScrapApiController {
    private final ScrapService scrapService;

    @PostMapping
    @ApiOperation(value = "스크랩 폴더 생성")
    public ResponseEntity<CommonApiResponse<ScrapFolderResponseDto>> makeScrapFolder(
            @ApiIgnore @LoginUser Member member,
            @RequestBody ScrapFolderRequestDto scrapFolderRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(scrapService.makeScrapFolder(member, scrapFolderRequestDto)));
    }

    @GetMapping
    @ApiOperation(value = "스크랩 폴더 조회")
    public ResponseEntity<CommonApiResponse<List<ScrapFolderResponseDto>>> showScrapFolders(
            @ApiIgnore @LoginUser Member member) {
        return ResponseEntity.ok(CommonApiResponse.of(scrapService.showScrapFolders(member)));
    }

    @PatchMapping("{scrapFolderId}")
    @ApiOperation(value = "스크랩 폴더 이름 수정")
    public ResponseEntity<CommonApiResponse<String>> modScrapFolderName(
            @PathVariable Long scrapFolderId,
            @RequestBody ScrapFolderRequestDto scrapFolderRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(scrapService.modScrapFolderName(scrapFolderId, scrapFolderRequestDto.getScrapFolderName())));
    }

    @DeleteMapping("{scrapFolderId}")
    @ApiOperation(value = "스크랩 폴더 삭제")
    public ResponseEntity<CommonApiResponse<Boolean>> delScrapFolder(
            @PathVariable Long scrapFolderId) {
        scrapService.delScrapFolder(scrapFolderId);
        return ResponseEntity.ok(CommonApiResponse.of(true));
    }

    @GetMapping("default")
    @ApiOperation(value = "기본 스크랩 폴더 조회")
    public ResponseEntity<CommonApiResponse<List<HomeResponseDto>>> showScrapDetails(
            @ApiIgnore @LoginUser Member member) {
        return ResponseEntity.ok(CommonApiResponse.of(scrapService.showScrapDetails(member)));
    }

    @PostMapping("default/{scrapFolderId}")
    @ApiOperation(value = "기본 스크랩 폴더에서 새 폴더로 이동")
    public ResponseEntity<CommonApiResponse<NewScrapFolderResponseDto>> makeNewFolderScrap(
            @PathVariable Long scrapFolderId,
            @ApiIgnore @LoginUser Member member,
            @RequestBody NewScrapFolderRequestDto newScrapFolderRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(scrapService.makeNewFolderScrap(member, scrapFolderId, newScrapFolderRequestDto.getCourseIds())));
    }

    @GetMapping("{scrapFolderId}")
    @ApiOperation(value = "새 폴더 속 코스 보기")
    public ResponseEntity<CommonApiResponse<List<HomeResponseDto>>> showNewScrapDetails(
            @PathVariable Long scrapFolderId) {
        return ResponseEntity.ok(CommonApiResponse.of(scrapService.showNewScrapDetails(scrapFolderId)));
    }

    @DeleteMapping("default/{courseIds}")
    @ApiOperation(value = "기본 스크랩 폴더에서 코스 삭제(스크랩 취소)")
    public ResponseEntity<CommonApiResponse<Boolean>> delScrap(
            @ApiIgnore @LoginUser Member member,
            @PathVariable List<Long> courseIds) {
        scrapService.delScrap(member, courseIds);
        return ResponseEntity.ok(CommonApiResponse.of(true));
    }

    @DeleteMapping("{scrapFolderId}/{courseIds}")
    @ApiOperation(value = "새 스크랩 폴더에서 코스 삭제")
    public ResponseEntity<CommonApiResponse<Boolean>> delNewScrap(
            @ApiIgnore @LoginUser Member member,
            @PathVariable Long scrapFolderId,
            @PathVariable List<Long> courseIds) {
        scrapService.delNewScrap(member, scrapFolderId, courseIds);
        return ResponseEntity.ok(CommonApiResponse.of(true));
    }
}
