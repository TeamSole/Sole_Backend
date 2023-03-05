package com.team6.sole.domain.home.entity;

import com.team6.sole.domain.home.model.PlaceCategory;
import com.team6.sole.domain.home.model.TransCategory;
import com.team6.sole.domain.home.model.WithCategory;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Set;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {
    @ElementCollection @Enumerated(EnumType.STRING)
    Set<PlaceCategory> placeCategories;

    @ElementCollection @Enumerated(EnumType.STRING)
    Set<WithCategory> withCategories;

    @ElementCollection @Enumerated(EnumType.STRING)
    Set<TransCategory> transCategories;

    @Builder

    public Category(Set<PlaceCategory> placeCategories, Set<WithCategory> withCategories, Set<TransCategory> transCategories) {
        this.placeCategories = placeCategories;
        this.withCategories = withCategories;
        this.transCategories = transCategories;
    }
}
