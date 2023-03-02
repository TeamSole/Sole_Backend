package com.team6.sole.domain.follow;

import com.team6.sole.domain.follow.event.FollowEvent;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.infra.notification.NotificationService;
import com.team6.sole.infra.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
@Async("follow")
@RequiredArgsConstructor
@Transactional
public class FollowEventListener {
    private final NotificationService notificationService;

    @EventListener
    public void handleFollowEvent(FollowEvent followEvent) {
        final Member receiver = followEvent.getToMember();

        final String title = followEvent.getFromMember().getNickname() + "님이 회원님의 발자국을 따라가기 시작했어요.";
        final String content = "현재 팔로워 수는 " + followEvent.getToMember().getFollowInfo().getFollower() + "명입니다.";

        if (!receiver.getFcmToken().isBlank() && receiver.getNotificationInfo().isActivityNot()) {
            notificationService.sendByToken(receiver.getFcmToken(), title, content);
        }

        // 알림 히스토리는 저장
        notificationService.createNotification(receiver, title, content, NotificationType.FOLLOW);
    }
}
