package com.team6.sole.domain.member.entity;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class NotificationInfo {
    private boolean activityNot;

    private boolean marketingNot;
}
