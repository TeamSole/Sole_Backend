package com.team6.sole.infra.notification;

import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.infra.notification.entity.Notification;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByReceiver(Member receiver, Sort sort);
}
