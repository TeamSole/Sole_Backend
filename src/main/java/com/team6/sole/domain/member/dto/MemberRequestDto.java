package com.team6.sole.domain.member.dto;

import com.team6.sole.domain.home.entity.Category;
import com.team6.sole.domain.home.entity.Gps;
import com.team6.sole.domain.home.model.PlaceCategory;
import com.team6.sole.domain.home.model.TransCategory;
import com.team6.sole.domain.home.model.WithCategory;
import com.team6.sole.domain.member.entity.Accept;
import com.team6.sole.domain.member.entity.FollowInfo;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.member.entity.NotificationInfo;
import com.team6.sole.domain.member.model.Role;
import com.team6.sole.domain.member.model.Social;
import com.team6.sole.domain.scrap.entity.ScrapFolder;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.ArrayList;
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

    @ApiModelProperty(value = "함께하는 사람 카테고리")
    private Set<WithCategory> withCategories;

    @ApiModelProperty(value = "대중교통 카테고리")
    private Set<TransCategory> transCategories;
    
    @ApiModelProperty(value = "소셜 어세스토큰")
    private String accessToken;

    @ApiModelProperty(value = "fcm 토큰")
    private String fcmToken;

    public static Accept acceptToEntity(MemberRequestDto memberRequestDto) {
        return Accept.builder()
            .serviceAccepted(memberRequestDto.isServiceAccepted())
            .infoAccepted(memberRequestDto.isInfoAccepted())
            .marketingAccepted(memberRequestDto.isMarketingAccepted())
            .locationAccepted(memberRequestDto.isLocationAccepted())
        .build();
    }

    public static Member memberToEntity(String socialCode, String password, Social social, String profileImgUrl, Accept accept, MemberRequestDto memberRequestDto) {
        return Member.builder()
                .socialId(socialCode)
                .password(password)
                .nickname(memberRequestDto.getNickname())
                .social(social)
                .role(Role.ROLE_USER)
                .profileImgUrl(profileImgUrl)
                .accept(accept)
                .favoriteCategory(
                        Category.builder()
                                .placeCategories(memberRequestDto.getPlaceCategories())
                                .withCategories(memberRequestDto.getWithCategories())
                                .transCategories(memberRequestDto.getTransCategories())
                                .build())
                .description("")
                .followInfo(
                        FollowInfo.builder()
                                .follower(0)
                                .following(0)
                                .build())
                .notificationInfo(
                        NotificationInfo.builder()
                                .activityNot(true)
                                .marketingNot(memberRequestDto.isMarketingAccepted())
                                .build()
                )
                .fromFollows(new ArrayList<>())
                .toFollows(new ArrayList<>())
                .fcmToken(
                        memberRequestDto.getFcmToken() == null
                                ? null
                                : memberRequestDto.getFcmToken())
                .currentGps(
                        Gps.builder()
                                .address("서울 마포구 마포대로 122")
                                .latitude(37.5453021) // 위도(x)
                                .longitude(126.952499) // 경도(y)
                                .distance(0)
                                .build())
                .build();
    }

    public static ScrapFolder scrapFolderToEntity(Member member, MemberRequestDto memberRequestDto) {
        return ScrapFolder.builder()
                .scrapFolderName("기본 폴더")
                .member(member)
                .build();
    }
}
