package com.team6.sole.domain.follow;

import com.team6.sole.domain.follow.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    void deleteByFollowId(Long followId);

    List<Follow> findByFromMember_SocialId(String socialId);

    List<Follow> findByToMember_SocialId(String socialId);

    boolean existsByFromMember_SocialIdAndToMember_SocialId(String fromMemberSocialId, String toMemberSocialId);

    boolean existsByFromMember_MemberIdAndToMember_MemberId(Long fromMemberId, Long toMemberId);

    Optional<Follow> findByFromMember_MemberIdAndToMember_MemberId(Long fromMemberId, Long toMemberId);

    void deleteByFromMember_MemberIdAndToMember_MemberId(Long fromMemberId, Long toMemberId);
}
