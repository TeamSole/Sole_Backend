package com.team6.sole.global.config.security;

import com.team6.sole.domain.member.MemberRepository;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.member.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;

@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String socialId) {
        return memberRepository.findBySocialId(socialId)
                .map(this::createUser)
                .orElseThrow(() -> new UsernameNotFoundException(socialId + " -> 데이터베이스에서 찾을 수 없습니다."));
    }

    private User createUser(Member member) {
        return new User(member.getSocialId(), member.getPassword(), authorities(member.getRole()));
    }

    private static Collection<? extends GrantedAuthority> authorities(Role userRole) {
        Collection<GrantedAuthority> role = new ArrayList<>();
        role.add(new SimpleGrantedAuthority(userRole.name()));
        return role;
    }
}
