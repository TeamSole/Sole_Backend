package com.team6.sole.domain.member.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Accept {
    @Id
    private boolean serviceAccepted;

    private boolean infoAccepted;

    private boolean marketingAccepted;

    @OneToOne(mappedBy = "accept", cascade = CascadeType.ALL)
    private Member member;

    @Builder
    public Accept(boolean serviceAccepted, boolean infoAccepted,
                  boolean marketingAccepted, Member member) {
        this.serviceAccepted = serviceAccepted;
        this.infoAccepted = infoAccepted;
        this.marketingAccepted = marketingAccepted;
        this.member = member;
    }
}
