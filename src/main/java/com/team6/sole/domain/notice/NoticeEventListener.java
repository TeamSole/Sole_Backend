package com.team6.sole.domain.notice;

import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.notice.event.NoticeEvent;
import com.team6.sole.infra.notification.NotificationService;
import com.team6.sole.infra.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class NoticeEventListener {
    private final NotificationService notificationService;

    @EventListener
    public void handleNoticeEvent(NoticeEvent noticeEvent) {
        final List<Member> receivers = noticeEvent.getReceivers();

        final String title = noticeEvent.getNoticeRequestDto().getTitle();
        final String content = noticeEvent.getNoticeRequestDto().getContent();

        for (Member receiver : receivers) {
            //FcmToken 비어있으면 로그아웃 또는 푸시알림 거부 -> 푸시 알림을 보내지 않음
            if (!receiver.getFcmToken().isBlank() && receiver.getNotificationInfo().isActivityNot()) {
                notificationService.sendByToken(receiver.getFcmToken(), title, content);
            }

            // 알림 히스토리는 저장
            notificationService.createNotification(receiver, title, content, NotificationType.NOTICE);
        }
    }
}
