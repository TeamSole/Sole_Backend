package com.team6.sole.domain.notice.entity;

import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.global.config.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    private String title;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member writer;

    public void modNotice(String title, String content) {
        this.title = title;
        this.content = content;
    }

    @Builder
    public Notice(Long noticeId, String title,
                  String content, Member writer) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.writer = writer;
    }
}
