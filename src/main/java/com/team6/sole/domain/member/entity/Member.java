package com.team6.sole.domain.member.entity;

import com.team6.sole.domain.follow.entity.Follow;
import com.team6.sole.domain.home.entity.Category;
import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.entity.Gps;
import com.team6.sole.domain.home.entity.relation.CourseMember;
import com.team6.sole.domain.member.model.Role;
import com.team6.sole.domain.member.model.Social;
import com.team6.sole.domain.notice.entity.Notice;
import com.team6.sole.domain.scrap.entity.ScrapFolder;
import com.team6.sole.global.config.entity.BaseTimeEntity;
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
public class Member extends BaseTimeEntity {
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

    @Embedded
    private Category favoriteCategory;

    @Embedded
    private Gps currentGps;

    @OneToOne(fetch = FetchType.LAZY)
    private Accept accept;

    @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Course> courses = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Course> recommendCourses = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CourseMember> courseMembers = new ArrayList<>();

    @OneToMany(mappedBy = "fromMember", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Follow> fromFollows = new ArrayList<>();

    @OneToMany(mappedBy = "toMember", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Follow> toFollows = new ArrayList<>();

    @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notice> notices = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScrapFolder> scrapFolders = new ArrayList<>();

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

    public void modFavCategory(Category favoriteCategory) {
        this.favoriteCategory = favoriteCategory;
    }

    public void setRecommendCourses(List<Course> recommendCourses) {
        this.recommendCourses = recommendCourses;
    }

    public void setCurrentGps(Gps currentGps) {
        this.currentGps = currentGps;
    }

    @Builder
    public Member(Long memberId, String socialId, String password,
                  String nickname, String profileImgUrl, String description, String fcmToken,
                  Social social, Role role,
                  FollowInfo followInfo, NotificationInfo notificationInfo, Category favoriteCategory, Gps currentGps,
                  Accept accept, List<Course> courses, List<Course> recommendCourses, List<CourseMember> courseMembers,
                  List<Follow> fromFollows, List<Follow> toFollows,
                  List<Notice> notices, List<Notification> notifications, List<ScrapFolder> scrapFolders) {
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
        this.favoriteCategory = favoriteCategory;
        this.currentGps = currentGps;
        this.accept = accept;
        this.courses = courses;
        this.recommendCourses = recommendCourses;
        this.courseMembers = courseMembers;
        this.fromFollows = fromFollows;
        this.toFollows = toFollows;
        this.notices = notices;
        this.notifications = notifications;
        this.scrapFolders = scrapFolders;
    }
}
