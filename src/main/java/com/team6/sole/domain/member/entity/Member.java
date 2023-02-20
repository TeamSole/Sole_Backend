package com.team6.sole.domain.member.entity;

import com.team6.sole.domain.member.model.Role;
import com.team6.sole.domain.member.model.Social;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    private String email;

    private String password;

    private String nickname;

    private String profileImgUrl;

    @Enumerated(EnumType.STRING)
    private Social social;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne
    private Accept accept;

    @Builder
    public Member(Long memberId, String email, String password,
                  String nickname, String profileImgUrl,
                  Social social, Role role, Accept accept) {
        this.memberId = memberId;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImgUrl = profileImgUrl;
        this.social = social;
        this.role = role;
        this.accept = accept;
    }
}
