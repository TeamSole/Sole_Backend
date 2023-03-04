package com.team6.sole.domain.member.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FollowInfo {
    private int follower;

    private int following;

    public void addFollower() {
        this.follower++;
    }

    public void addFollowing() {
        this.following++;
    }

    public void removeFollower() {
        this.follower--;
    }

    public void removeFollowing() {
        this.following--;
    }

    @Builder
    public FollowInfo(int follower, int following) {
        this.follower = follower;
        this.following = following;
    }
}
