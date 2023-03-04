package com.team6.sole.infra.notification.entity;

import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.global.config.entity.BaseTimeEntity;
import com.team6.sole.infra.notification.model.NotificationType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    private String title;

    private String content;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member receiver;

    @Builder
    public Notification(Long notificationId, String title,
                        String content, NotificationType type, Member receiver) {
        this.notificationId = notificationId;
        this.title = title;
        this.content = content;
        this.type = type;
        this.receiver = receiver;
    }
}
