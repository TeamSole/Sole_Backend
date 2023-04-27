package com.team6.sole.domain.scrap;

import com.team6.sole.domain.home.dto.HomeResponseDto;
import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.home.repository.CourseMemberRepository;
import com.team6.sole.domain.home.repository.CourseRepository;
import com.team6.sole.domain.member.MemberRepository;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.scrap.dto.NewScrapFolderResponseDto;
import com.team6.sole.domain.scrap.dto.ScrapFolderResponseDto;
import com.team6.sole.domain.scrap.dto.ScrapFolderRequestDto;
import com.team6.sole.domain.scrap.entity.CourseMemberScrapFolder;
import com.team6.sole.domain.scrap.entity.ScrapFolder;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.BadRequestException;
import com.team6.sole.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScrapService {
    private final ScrapFolderRespository scrapFolderRespository;
    private final MemberRepository memberRepository;
    private final CourseMemberRepository courseMemberRepository;
    private final CourseMemberScrapFolderRepository courseMemberScrapFolderRepository;
    private final CourseRepository courseRepository;

    // 스크랩 폴더 추가
    @Transactional
    public ScrapFolderResponseDto makeScrapFolder(Member member, ScrapFolderRequestDto scrapFolderRequestDto) {
        ScrapFolder scrapFolder = ScrapFolder.builder()
                .scrapFolderName(scrapFolderRequestDto.getScrapFolderName())
                .courseMemberScrapFolders(new ArrayList<>())
                .member(member)
                .build();
        scrapFolderRespository.save(scrapFolder);

        return ScrapFolderResponseDto.of(scrapFolder);
    }

    // 스크랩 폴더 보기
    @Transactional(readOnly = true)
    public List<ScrapFolderResponseDto> showScrapFolders(Member member) {
        List<ScrapFolder> scrapFolders = scrapFolderRespository.findByMember_SocialId(member.getSocialId());

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
    public List<HomeResponseDto> showScrapDetails(Member member) {
        List<CourseMember> courses = courseMemberRepository.findByMember_SocialId(member.getSocialId());

        return courses.stream()
                .map(course -> HomeResponseDto.of(course.getCourse(), true, false))
                .collect(Collectors.toList());
    }

    // 기본 폴더에서 새 폴더로 이동
    @Transactional
    public NewScrapFolderResponseDto makeNewFolderScrap(Member member, Long scrapFolderId, List<Long> courseIds) {
        ScrapFolder scrapFolder = scrapFolderRespository.findById(scrapFolderId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.SCRAP_FOLDER_NOT_FOUND));
        List<CourseMember> courseMembers = courseMemberRepository.findAllByMember_SocialIdAndCourse_CourseIdIn(member.getSocialId(), courseIds);

        for (CourseMember scrap : courseMembers) {
            if (courseMemberScrapFolderRepository.existsByScrapFolderAndCourseMember(scrapFolder, scrap)
                    || scrapFolder.getScrapFolderName().equals("기본 폴더")) {
                throw new BadRequestException(ErrorCode.ALREADY_SCRAPED);
            }

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
    public void delScrap(Member member, List<Long> courseIds) {
        for (Long courseId : courseIds) {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new NotFoundException(ErrorCode.COURSE_NOT_FOUND));

            courseMemberRepository.deleteByCourse_CourseIdAndMember_SocialId(courseId, member.getSocialId());
            course.removeScrapCount();
        }
    }

    // 새 폴더에서 코스 삭제(스크랩 수 변동 없음)
    @Transactional
    public void delNewScrap(Member member, Long scrapFolderId, List<Long> courseIds) {
        for (Long courseId : courseIds) {
            CourseMember courseMember = courseMemberRepository.findByCourse_CourseIdAndMember_SocialId(courseId, member.getSocialId());

            courseMemberScrapFolderRepository.deleteByCourseMemberAndScrapFolder_ScrapFolderId(courseMember, scrapFolderId);
        }
    }
}
