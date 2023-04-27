package com.team6.sole.domain.member.dto;

import com.team6.sole.domain.home.model.PlaceCategory;
import com.team6.sole.domain.home.model.TransCategory;
import com.team6.sole.domain.home.model.WithCategory;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

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

    @ApiModelProperty(value = "위치정보 동의", example = "true")
    private boolean locationAccepted;

    @ApiModelProperty(value = "장소 카테고리")
    private Set<PlaceCategory> placeCategories;

    @ApiModelProperty(value = "함께하는 사람 카테로기")
    private Set<WithCategory> withCategories;

    @ApiModelProperty(value = "대중교통 카테고리")
    private Set<TransCategory> transCategories;
    
    @ApiModelProperty(value = "소셜 어세스토큰")
    private String accessToken;

    @ApiModelProperty(value = "fcm 토큰")
    private String fcmToken;
}
