package com.team6.sole.domain.scrap;

import com.team6.sole.domain.home.dto.HomeResponseDto;
import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.home.repository.CourseMemberRepository;
import com.team6.sole.domain.home.repository.CourseRepository;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.scrap.dto.NewScrapFolderResponseDto;
import com.team6.sole.domain.scrap.dto.ScrapFolderResponseDto;
import com.team6.sole.domain.scrap.dto.ScrapFolderRequestDto;
import com.team6.sole.domain.scrap.entity.ScrapFolder;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScrapService {
    private final ScrapFolderRespository scrapFolderRespository;
    private final CourseMemberRepository courseMemberRepository;
    private final CourseRepository courseRepository;

    // 스크랩 폴더 추가
    @Transactional
    public ScrapFolderResponseDto makeScrapFolder(Member member, ScrapFolderRequestDto scrapFolderRequestDto) {
        ScrapFolder scrapFolder = ScrapFolderRequestDto.scrapFolderToEntity(member, scrapFolderRequestDto);
        scrapFolderRespository.save(scrapFolder);

        return ScrapFolderResponseDto.of(scrapFolder);
    }

    // 스크랩 폴더 보기
    @Transactional(readOnly = true)
    public List<ScrapFolderResponseDto> showScrapFolders(Member member) {
        List<ScrapFolder> scrapFolders = scrapFolderRespository.findByMember_SocialId(member.getSocialId());

        return makeScrapFoldersToDto(scrapFolders);
    }

    // Entity To Dto
    public List<ScrapFolderResponseDto> makeScrapFoldersToDto(List<ScrapFolder> scrapFolders) {
        return scrapFolders.stream()
                .map(ScrapFolderResponseDto::of)
                .collect(Collectors.toList());
    }
    
    // 스크랩 폴더 이름 수정
    @Transactional
    public String modScrapFolderName(Long scrapFolderId, String scrapFolderName) {
        ScrapFolder scrapFolder = scrapFolderRespository.findById(scrapFolderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SCRAP_FOLDER_NOT_FOUND));
        scrapFolder.modScrapFolderName(scrapFolderName);

        return scrapFolder.getScrapFolderName() + "으로 수정되었습니다.";
    }

    // 스크랩 폴더 삭제
    @Transactional
    public void delScrapFolder(Long scrapFolderId) {
        ScrapFolder scrapFolder = scrapFolderRespository.findById(scrapFolderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SCRAP_FOLDER_NOT_FOUND));

        // 스크랩 폴더에 있는 코스들의 스크랩 수를 감소시킨다.
        removeCourseScrapCount(scrapFolder);
        scrapFolderRespository.deleteByScrapFolderId(scrapFolderId);
    }

    // 스크랩 폴더에 있는 코스들 스크랩 수 감소
    public void removeCourseScrapCount(ScrapFolder scrapFolder) {
        for (CourseMember courseMember : scrapFolder.getCourseMembers()) {
            courseMember.getCourse().removeScrapCount();
        }
    }

    // 폴더에서 폴더로 이동
    @Transactional
    public NewScrapFolderResponseDto makeNewFolderScrap(Member member, Long fromScrapFolderId, Long toScrapFolderId, List<Long> courseIds) {
        ScrapFolder scrapFolder = scrapFolderRespository.findById(toScrapFolderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SCRAP_FOLDER_NOT_FOUND));
        List<CourseMember> courseMembers = courseMemberRepository.findAllByMemberAndScrapFolder_ScrapFolderIdAndCourse_CourseIdIn(member, fromScrapFolderId, courseIds);

        // 중복 스크랩
        /*for (CourseMember scrap : courseMembers) {
            if (new HashSet<>(courseMembers.stream()
                    .map(CourseMember::getCourse)
                    .collect(Collectors.toList()))
                    .containsAll(scrapFolder.getCourseMembers().stream()
                            .map(CourseMember::getCourse)
                            .collect(Collectors.toList()))) {
                throw new BadRequestException(ErrorCode.ALREADY_SCRAPED);
            }

            scrap.modScrapFolder(scrapFolder);
        }*/
        courseMembers.forEach(scrap -> scrap.modScrapFolder(scrapFolder));

        return NewScrapFolderResponseDto.of(scrapFolder.getScrapFolderName(), courseMembers);
    }

    // 폴더 속 코스 보기
    @Transactional(readOnly = true)
    public List<HomeResponseDto> showScrapDetails(Member member, Long scrapFolderId) {
        List<CourseMember> courseMembers = courseMemberRepository.findByMemberAndScrapFolder_ScrapFolderId(member, scrapFolderId);

        return makeScrapDetailsToDto(courseMembers);
    }

    public List<HomeResponseDto> makeScrapDetailsToDto(List<CourseMember> courseMembers) {
        return courseMembers.stream()
                .map(newScrap -> HomeResponseDto.of(newScrap.getCourse(), true, false))
                .collect(Collectors.toList()); 
    }

    // 폴더에서 코스 삭제(스크랩 수 - 1)
    @Transactional
    public void delScrap(Member member, Long scrapFolderId, List<Long> courseIds) {
        for (Long courseId : courseIds) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));

            courseMemberRepository.deleteByCourseAndScrapFolder_ScrapFolderIdAndMember(course, scrapFolderId, member);
            course.removeScrapCount();
        }
    }
}
