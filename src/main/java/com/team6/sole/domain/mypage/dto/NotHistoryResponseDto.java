package com.team6.sole.domain.mypage.dto;

import com.team6.sole.global.util.Time;
import com.team6.sole.infra.notification.entity.Notification;
import com.team6.sole.infra.notification.model.NotificationType;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotHistoryResponseDto {
    private String title;

    private String content;

    private NotificationType type;

    private String createdAt;

    @Builder
    public NotHistoryResponseDto(String title, String content,
                                 NotificationType type, String createdAt) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.createdAt = createdAt;
    }

    public static NotHistoryResponseDto of(Notification notification) {
        return NotHistoryResponseDto.builder()
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .createdAt(Time.calculateTime(Timestamp.valueOf(notification.getCreatedAt())))
                .build();
    }
}
