package com.team6.sole.domain.scrap;

import com.team6.sole.domain.home.dto.HomeResponseDto;
import com.team6.sole.domain.scrap.dto.NewScrapFolderRequestDto;
import com.team6.sole.domain.scrap.dto.NewScrapFolderResponseDto;
import com.team6.sole.domain.scrap.dto.ScrapFolderResponseDto;
import com.team6.sole.domain.scrap.dto.ScrapFolderRequestDto;
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
@Api(tags = "스크랩")
@RequestMapping("api/scraps")
public class ScrapApiController {
    private final ScrapService scrapService;

    @PostMapping
    @ApiOperation(value = "스크랩 폴더 생성")
    public ResponseEntity<CommonApiResponse<ScrapFolderResponseDto>> makeScrapFolder(
            @ApiIgnore Authentication authentication,
            @RequestBody ScrapFolderRequestDto scrapFolderRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(scrapService.makeScrapFolder(authentication.getName(), scrapFolderRequestDto)));
    }

    @GetMapping
    @ApiOperation(value = "스크랩 폴더 조회")
    public ResponseEntity<CommonApiResponse<List<ScrapFolderResponseDto>>> showScrapFolders(
            @ApiIgnore Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(scrapService.showScrapFolders(authentication.getName())));
    }

    @GetMapping("default")
    @ApiOperation(value = "기본 스크랩 폴더 조회")
    public ResponseEntity<CommonApiResponse<List<HomeResponseDto>>> showScrapDetails(
            @ApiIgnore Authentication authentication) {
        return ResponseEntity.ok(CommonApiResponse.of(scrapService.showScrapDetails(authentication.getName())));
    }

    @PostMapping("default/{scrapFolderId}")
    @ApiOperation(value = "기본 스크랩 폴더에서 새 폴더로 이동")
    public ResponseEntity<CommonApiResponse<NewScrapFolderResponseDto>> makeNewFolderScrap(
            @PathVariable Long scrapFolderId,
            @RequestBody NewScrapFolderRequestDto newScrapFolderRequestDto) {
        return ResponseEntity.ok(CommonApiResponse.of(scrapService.makeNewFolderScrap(scrapFolderId, newScrapFolderRequestDto.getCourseIds())));
    }

    @GetMapping("{scrapFolderId}")
    @ApiOperation(value = "새 폴더 속 코스 보기")
    public ResponseEntity<CommonApiResponse<List<HomeResponseDto>>> showNewScrapDetails(
            @PathVariable Long scrapFolderId) {
        return ResponseEntity.ok(CommonApiResponse.of(scrapService.showNewScrapDetails(scrapFolderId)));
    }

    @DeleteMapping("default/{courseId}")
    @ApiOperation(value = "기본 스크랩 폴더에서 코스 삭제(스크랩 취소)")
    public ResponseEntity<Void> delScrap(
            @ApiIgnore Authentication authentication,
            @PathVariable Long courseId) {
        scrapService.delScrap(authentication.getName(), courseId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{scrapFolderId}/{courseId}")
    @ApiOperation(value = "새 스크랩 폴더에서 코스 삭제")
    public ResponseEntity<Void> delNewScrap(
            @ApiIgnore Authentication authentication,
            @PathVariable Long scrapFolderId,
            @PathVariable Long courseId) {
        scrapService.delNewScrap(authentication.getName(), scrapFolderId, courseId);
        return ResponseEntity.ok().build();
    }
}
