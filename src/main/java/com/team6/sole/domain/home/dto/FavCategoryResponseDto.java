package com.team6.sole.domain.home.dto;

import com.team6.sole.domain.home.model.PlaceCategory;
import com.team6.sole.domain.home.model.TransCategory;
import com.team6.sole.domain.home.model.WithCategory;
import com.team6.sole.domain.member.entity.Member;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavCategoryResponseDto {
    private Long memberId;

    private String nickname;

    private Set<PlaceCategory> placeCategories;

    private Set<TransCategory> transCategories;

    private Set<WithCategory> withCategories;

    @Builder
    public FavCategoryResponseDto(Long memberId, String nickname,
                                  Set<PlaceCategory> placeCategories,
                                  Set<TransCategory> transCategories,
                                  Set<WithCategory> withCategories) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.placeCategories = placeCategories;
        this.transCategories = transCategories;
        this.withCategories = withCategories;
    }

    public static FavCategoryResponseDto of(Member member) {
        return FavCategoryResponseDto.builder()
                .memberId(member.getMemberId())
                .nickname(member.getNickname())
                .placeCategories(member.getFavoriteCategory().getPlaceCategories())
                .transCategories(member.getFavoriteCategory().getTransCategories())
                .withCategories(member.getFavoriteCategory().getWithCategories())
                .build();
    }
}
