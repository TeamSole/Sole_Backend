package com.team6.sole.infra.notification;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.InternalServerException;
import com.team6.sole.infra.notification.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    @Value("${fcm.key.path}")
    private String FCM_PRIVATE_KEY_PATH;

    // 메시징만 권한 설정
    @Value("${fcm.key.scope}")
    private String fireBaseScope;

    private final NotificationRepository notificationRepository;

    // fcm 기본 설정 진행
    @PostConstruct
    public void init() {
        try {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(
                            GoogleCredentials
                                    .fromStream(new ClassPathResource(FCM_PRIVATE_KEY_PATH).getInputStream())
                                    .createScoped(List.of(fireBaseScope)))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            // spring 뜰때 알림 서버가 잘 동작하지 않는 것이므로 바로 죽임
            throw new RuntimeException(e.getMessage());
        }
    }

    public void sendByToken(String token, String title, String content) {

        // 메시지 만들기
        Message message = Message.builder()
                .putData("time", LocalDateTime.now().toString())
                .setNotification(new Notification(title, content))
                .setToken(token)
                .build();

        try {
            // 알림 발송
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("cannot send to memberList push message. error info : {}", e.getMessage());
        }
    }

    public void createNotification(Member receiver, String title, String content, NotificationType notificationType) {
        try {
            notificationRepository.save(
                    com.team6.sole.infra.notification.entity.Notification.builder()
                            .receiver(receiver)
                            .title(title)
                            .content(content)
                            .type(notificationType)
                            .build()
            );
        } catch (Exception e) {
            throw new InternalServerException(ErrorCode.CANNOT_CREATE_TUPLE);
        }
    }

    public void createNotificationList(List<Member> receivers, String title, String content, NotificationType notificationType) {
        try {
            notificationRepository.saveAll(receivers.stream().map(receiver ->
                    com.team6.sole.infra.notification.entity.Notification.builder()
                            .receiver(receiver)
                            .title(title)
                            .content(content)
                            .type(notificationType)
                            .build()
            ).collect(Collectors.toList()));
        } catch (Exception e) {
            throw new InternalServerException(ErrorCode.CANNOT_CREATE_TUPLE);
        }
    }

    // 알림 보내기
    public void sendByTokenList(List<String> tokenList) {

        // 메시지 만들기
        List<Message> messages = tokenList.stream().map(token -> Message.builder()
                .putData("time", LocalDateTime.now().toString())
                .setNotification(new Notification("제목", "알림 내용"))
                .setToken(token)
                .build()).collect(Collectors.toList());

        // 요청에 대한 응답을 받을 response
        BatchResponse response;
        try {

            // 알림 발송
            response = FirebaseMessaging.getInstance().sendAll(messages);

            // 요청에 대한 응답 처리
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                List<String> failedTokens = new ArrayList<>();

                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        failedTokens.add(tokenList.get(i));
                    }
                }
                log.error("List of tokens are not valid FCM token : " + failedTokens);
            }
        } catch (FirebaseMessagingException e) {
            log.error("cannot send to memberList push message. error info : {}", e.getMessage());
        }
    }
}
