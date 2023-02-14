package com.team6.sole.domain.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRequestDto {
    @ApiModelProperty(value = "닉네임", example = "지미")
    @NotNull
    @Size(max = 10)
    private String nickname;

    @ApiModelProperty(value = "서비스 이용약관 동의", example = "true")
    @NotNull
    private boolean serviceAccepted;

    @ApiModelProperty(value = "개인정보 처리방침 동의", example = "true")
    @NotNull
    private boolean infoAccepted;

    @ApiModelProperty(value = "마케팅 정보 수신 및 동의", example = "false")
    private boolean marketingAccepted;
}
