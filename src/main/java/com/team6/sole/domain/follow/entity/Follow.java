package com.team6.sole.domain.follow.entity;

import com.team6.sole.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Follow {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member fromMember;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member toMember;

    @Builder
    public Follow(Long followId, Member fromMember, Member toMember) {
        this.followId = followId;
        this.fromMember = fromMember;
        this.toMember = toMember;
    }
}
