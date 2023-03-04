package com.team6.sole.domain.follow;

import com.team6.sole.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    void deleteByFollowId(Long followId);

    List<Follow> findByFromMember_SocialId(String socialId);

    int countByToMember_MemberId(Long toMemberId);

    int countByFromMember_MemberId(Long fromMemberId);
}
