package com.team6.sole.domain.scrap;

import com.team6.sole.domain.home.dto.HomeResponseDto;
import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.home.repository.CourseMemberRepository;
import com.team6.sole.domain.home.repository.CourseRepository;
import com.team6.sole.domain.member.MemberRepository;
import com.team6.sole.domain.scrap.dto.NewScrapFolderResponseDto;
import com.team6.sole.domain.scrap.dto.ScrapFolderResponseDto;
import com.team6.sole.domain.scrap.dto.ScrapFolderRequestDto;
import com.team6.sole.domain.scrap.entity.CourseMemberScrapFolder;
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
    private final MemberRepository memberRepository;
    private final CourseMemberRepository courseMemberRepository;
    private final CourseMemberScrapFolderRepository courseMemberScrapFolderRepository;
    private final CourseRepository courseRepository;

    // 스크랩 폴더 추가
    @Transactional
    public ScrapFolderResponseDto makeScrapFolder(String socialId, ScrapFolderRequestDto scrapFolderRequestDto) {
        ScrapFolder scrapFolder = ScrapFolder.builder()
                .scrapFolderName(scrapFolderRequestDto.getScrapFolderName())
                .member(memberRepository.findBySocialId(socialId)
                        .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND)))
                .build();
        scrapFolderRespository.save(scrapFolder);

        return ScrapFolderResponseDto.of(scrapFolder);
    }

    // 스크랩 폴더 보기
    @Transactional(readOnly = true)
    public List<ScrapFolderResponseDto> showScrapFolders(String socialId) {
        List<ScrapFolder> scrapFolders = scrapFolderRespository.findByMember_SocialId(socialId);

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
        scrapFolderRespository.deleteByScrapFolderId(scrapFolderId);
    }

    // 기본 폴더 속 코스 보기
    @Transactional(readOnly = true)
    public List<HomeResponseDto> showScrapDetails(String socialId) {
        List<CourseMember> courses = courseMemberRepository.findByMember_SocialId(socialId);

        return courses.stream()
                .map(course -> HomeResponseDto.of(course.getCourse(), true, false))
                .collect(Collectors.toList());
    }

    // 기본 폴더에서 새 폴더로 이동
    @Transactional
    public NewScrapFolderResponseDto makeNewFolderScrap(Long scrapFolderId, List<Long> courseIds) {
        ScrapFolder scrapFolder = scrapFolderRespository.findById(scrapFolderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SCRAP_FOLDER_NOT_FOUND));
        List<CourseMember> courseMembers = courseMemberRepository.findAllByCourse_CourseIdIn(courseIds);

        for (CourseMember scrap : courseMembers) {
            CourseMemberScrapFolder courseMemberScrapFolder = CourseMemberScrapFolder.builder()
                    .scrapFolder(scrapFolder)
                    .courseMember(scrap)
                    .build();
            courseMemberScrapFolderRepository.save(courseMemberScrapFolder);
        }

        return NewScrapFolderResponseDto.of(scrapFolder.getScrapFolderName(), courseMembers);
    }

    // 새 폴더 속 코스 보기
    @Transactional(readOnly = true)
    public List<HomeResponseDto> showNewScrapDetails(Long scrapFolderId) {
        List<CourseMemberScrapFolder> courseMemberScrapFolders = courseMemberScrapFolderRepository
                .findAllByScrapFolder_ScrapFolderId(scrapFolderId);

        return courseMemberScrapFolders.stream()
                .map(newScrap -> HomeResponseDto.of(newScrap.getCourseMember().getCourse(), true, false))
                .collect(Collectors.toList());
    }

    // 기본 폴더에서 코스 삭제(스크랩 수 -1)
    @Transactional
    public void delScrap(String socialId, List<Long> courseIds) {
        for (Long courseId : courseIds) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));

            courseMemberRepository.deleteByCourse_CourseIdAndMember_SocialId(courseId, socialId);
            course.removeScrapCount();
        }
    }

    // 새 폴더에서 코스 삭제(스크랩 수 변동 없음)
    @Transactional
    public void delNewScrap(String socialId, Long scrapFolderId, List<Long> courseIds) {
        for (Long courseId : courseIds) {
            CourseMember courseMember = courseMemberRepository.findByCourse_CourseIdAndMember_SocialId(courseId, socialId);

            courseMemberScrapFolderRepository.deleteByCourseMemberAndScrapFolder_ScrapFolderId(courseMember, scrapFolderId);
        }
    }
}
