package com.team6.sole.domain.follow.event;

import com.team6.sole.domain.member.entity.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class FollowEvent {
    private final Member fromMember;
    private final Member toMember;
}
