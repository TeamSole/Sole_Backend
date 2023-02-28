package com.team6.sole.domain.notice.dto;

import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.notice.entity.Notice;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeResponseDto {
    private Long noticeId;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    private Member writer;

    @Builder
    public NoticeResponseDto(Long noticeId, String title, String content,
                             LocalDateTime createdAt, Member writer) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.writer = writer;
    }

    public static NoticeResponseDto of(Notice notice) {
        return NoticeResponseDto.builder()
                .noticeId(notice.getNoticeId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt())
                .writer(notice.getWriter())
                .build();
    }
}
