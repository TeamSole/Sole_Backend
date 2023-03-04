package com.team6.sole.domain.member;

import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.member.model.Role;
import com.team6.sole.domain.member.model.Social;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findBySocialId(String socialId);

    Optional<Member> findBySocialIdAndSocial(String socialId, Social social);

    boolean existsByNickname(String nickname);

    void deleteByMemberId(Long memberId);

    List<Member> findAllByRoleAndNotificationInfo_ActivityNotTrue(Role role);
}
