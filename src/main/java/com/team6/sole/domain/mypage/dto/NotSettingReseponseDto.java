package com.team6.sole.domain.mypage.dto;

import com.team6.sole.domain.member.entity.Member;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotSettingReseponseDto {
    private String nickname;

    private boolean activityNot;

    private boolean marketingNot;

    @Builder
    public NotSettingReseponseDto(String nickname, boolean activityNot, boolean marketingNot) {
        this.nickname = nickname;
        this.activityNot = activityNot;
        this.marketingNot = marketingNot;
    }

    public static NotSettingReseponseDto of(Member member) {
        return NotSettingReseponseDto.builder()
                .nickname(member.getNickname())
                .activityNot(member.getNotificationInfo().isActivityNot())
                .marketingNot(member.getNotificationInfo().isMarketingNot())
                .build();
    }
}
