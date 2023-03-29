package com.team6.sole.domain.notice;

import com.team6.sole.domain.member.MemberRepository;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.member.model.Role;
import com.team6.sole.domain.notice.dto.NoticeRequestDto;
import com.team6.sole.domain.notice.dto.NoticeResponseDto;
import com.team6.sole.domain.notice.entity.Notice;
import com.team6.sole.domain.notice.event.NoticeEvent;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.NotFoundException;
import com.team6.sole.infra.notification.NotificationService;
import com.team6.sole.infra.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoticeService {
    private final NoticeRepository noticeRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final NotificationService notificationService;

    // 공지사항 등록
    @Transactional
    public NoticeResponseDto makeNotice(Member writer, NoticeRequestDto noticeRequestDto) {
        Notice notice = Notice.builder()
                .title(noticeRequestDto.getTitle())
                .content(noticeRequestDto.getContent())
                .writer(writer)
                .build();
        noticeRepository.save(notice);

        List<Member> receivers = memberRepository.findAllByRoleAndNotificationInfo_ActivityNotTrue(Role.ROLE_USER);

        //알림 이벤트 전송(fcm)
        try {
            applicationEventPublisher.publishEvent(new NoticeEvent(receivers, noticeRequestDto));
        } catch (Exception e) {
            log.error("푸시 알림 전송에 실패했습니다 - {}", e.getMessage());
        }

        return NoticeResponseDto.of(notice);
    }
    
    // 공지사항 조회
    @Transactional(readOnly = true)
    @Cacheable(value = "notices")
    public List<NoticeResponseDto> showNotices() {
        List<Notice> notices = noticeRepository.findAll();
        
        return notices.stream()
                .map(NoticeResponseDto::of)
                .collect(Collectors.toList());
    }
    
    // 공지사항 상세조회
    @Transactional(readOnly = true)
    @Cacheable(value = "notices", key = "#noticeId")
    public NoticeResponseDto showNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTICE_NOT_FOUND));

        return NoticeResponseDto.of(notice);
    }
    
    // 공지사항 수정
    @Transactional
    @CacheEvict(value = "notices", allEntries = true)
    public NoticeResponseDto modNotice(Long noticeId, NoticeRequestDto noticeRequestDto) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTICE_NOT_FOUND));

        notice.modNotice(noticeRequestDto.getTitle(), noticeRequestDto.getContent());

        return NoticeResponseDto.of(notice);
    }

    // 알림 테스트
    @Transactional
    public String test(Member member) {
        notificationService.createNotification(member, "테스트", "테스트", NotificationType.MARKETING);

        return "알림 테스트 성공...!";
    }
}
