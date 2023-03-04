package com.team6.sole.domain.mypage.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotSettingRequestDto {
    private boolean activityNot;

    private boolean marketingNot;
}
