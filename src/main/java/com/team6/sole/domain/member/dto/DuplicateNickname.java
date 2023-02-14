package com.team6.sole.domain.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DuplicateNickname {
    @ApiModelProperty(value = "중복된 닉네임", example = "지미")
    private String nickname;
}
