package com.team6.sole.domain.notice;

import com.team6.sole.domain.member.MemberRepository;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.notice.dto.NoticeRequestDto;
import com.team6.sole.domain.notice.dto.NoticeResponseDto;
import com.team6.sole.domain.notice.entity.Notice;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;

    // 공지사항 등록
    @Transactional
    public NoticeResponseDto makeNotice(String socialId, NoticeRequestDto noticeRequestDto) {
        Member writer = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        Notice notice = Notice.builder()
                .title(noticeRequestDto.getTitle())
                .content(noticeRequestDto.getContent())
                .writer(writer)
                .build();
        noticeRepository.save(notice);

        /*fcm 추가...!*/

        return NoticeResponseDto.of(notice);
    }
    
    // 공지사항 조회
    @Transactional(readOnly = true)
    public List<NoticeResponseDto> showNotices() {
        List<Notice> notices = noticeRepository.findAll();
        
        return notices.stream()
                .map(NoticeResponseDto::of)
                .collect(Collectors.toList());
    }
    
    // 공지사항 상세조회
    @Transactional(readOnly = true)
    public NoticeResponseDto showNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTICE_NOT_FOUND));

        return NoticeResponseDto.of(notice);
    }
}
