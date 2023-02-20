package com.team6.sole.domain.member.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Accept {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long acceptId;

    private boolean serviceAccepted;

    private boolean infoAccepted;

    private boolean marketingAccepted;

    @OneToOne(mappedBy = "accept", cascade = CascadeType.ALL)
    private Member member;

    @Builder
    public Accept(Long acceptId, boolean serviceAccepted, boolean infoAccepted,
                  boolean marketingAccepted, Member member) {
        this.acceptId = acceptId;
        this.serviceAccepted = serviceAccepted;
        this.infoAccepted = infoAccepted;
        this.marketingAccepted = marketingAccepted;
        this.member = member;
    }
}
