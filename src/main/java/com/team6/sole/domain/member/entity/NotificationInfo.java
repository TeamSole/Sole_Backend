package com.team6.sole.domain.member.entity;

import lombok.*;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationInfo {
    private boolean activityNot;

    private boolean marketingNot;

    @Builder
    public NotificationInfo(boolean activityNot, boolean marketingNot) {
        this.activityNot = activityNot;
        this.marketingNot = marketingNot;
    }
}
