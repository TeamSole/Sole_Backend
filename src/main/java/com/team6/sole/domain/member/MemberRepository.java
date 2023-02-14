package com.team6.sole.domain.member;

import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.member.model.Social;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    Optional<Member> findByEmailAndSocial(String email, Social social);
}
