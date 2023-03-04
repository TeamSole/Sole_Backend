package com.team6.sole.domain.member.entity;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
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
