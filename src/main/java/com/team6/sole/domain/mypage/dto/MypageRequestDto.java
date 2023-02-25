package com.team6.sole.domain.mypage.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MypageRequestDto {
    @ApiModelProperty(value = "닉네임", example = "지미1234")
    @Size(max = 10)
    private String nickname;
    @ApiModelProperty(value = "한 줄 소개", example = "안녕하세요 지미입니다.")
    @Size(max = 50)
    private String description;
}
