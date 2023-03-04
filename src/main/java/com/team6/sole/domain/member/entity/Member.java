package com.team6.sole.domain.member.entity;

import com.team6.sole.domain.follow.entity.Follow;
import com.team6.sole.domain.member.model.Role;
import com.team6.sole.domain.member.model.Social;
import com.team6.sole.domain.notice.entity.Notice;
import com.team6.sole.infra.notification.entity.Notification;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    private String socialId;

    private String password;

    private String nickname;

    private String profileImgUrl;

    private String description;

    private String fcmToken;

    @Enumerated(EnumType.STRING)
    private Social social;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Embedded
    private FollowInfo followInfo;

    @Embedded
    private NotificationInfo notificationInfo;

    @OneToOne(fetch = FetchType.LAZY)
    private Accept accept;

    @OneToMany(mappedBy = "fromMember", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Follow> fromFollows = new ArrayList<>();

    @OneToMany(mappedBy = "toMember", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Follow> toFollows = new ArrayList<>();

    @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notice> notices = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications = new ArrayList<>();

    public void modMypage(String profileImgUrl, String nickname, String description) {
        this.profileImgUrl = profileImgUrl;
        this.nickname = nickname;
        this.description = description;
    }

    public void modNotSetting(boolean activityNot, boolean marketingNot) {
        this.notificationInfo.setActivityNot(activityNot);
        this.notificationInfo.setMarketingNot(marketingNot);
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    @Builder
    public Member(Long memberId, String socialId, String password,
                  String nickname, String profileImgUrl, String description, String fcmToken,
                  Social social, Role role, FollowInfo followInfo, NotificationInfo notificationInfo,
                  Accept accept, List<Follow> fromFollows, List<Follow> toFollows,
                  List<Notice> notices, List<Notification> notifications) {
        this.memberId = memberId;
        this.socialId = socialId;
        this.password = password;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.description = description;
        this.fcmToken = fcmToken;
        this.social = social;
        this.role = role;
        this.followInfo = followInfo;
        this.notificationInfo = notificationInfo;
        this.accept = accept;
        this.fromFollows = fromFollows;
        this.toFollows = toFollows;
        this.notices = notices;
        this.notifications = notifications;
    }
}
