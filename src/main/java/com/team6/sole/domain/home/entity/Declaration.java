package com.team6.sole.domain.home.entity;

import com.team6.sole.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Declaration {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long declarationId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Builder
    public Declaration(Long declarationId, Course course, Member member) {
        this.declarationId = declarationId;
        this.course = course;
        this.member = member;
    }
}
