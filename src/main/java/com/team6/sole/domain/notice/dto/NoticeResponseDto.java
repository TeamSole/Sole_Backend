package com.team6.sole.domain.notice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.team6.sole.domain.member.dto.MemberResponseDto;
import com.team6.sole.domain.notice.entity.Notice;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL) //NULL 필드 가림
public class NoticeResponseDto {
    private Long noticeId;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    private MemberResponseDto writer;

    @Builder
    public NoticeResponseDto(Long noticeId, String title, String content,
                             LocalDateTime createdAt, MemberResponseDto writer) {
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
                .writer(MemberResponseDto.of(notice.getWriter()))
                .build();
    }
}
