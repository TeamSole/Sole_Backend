package com.team6.sole.domain.follow;

import com.team6.sole.domain.follow.dto.FollowResponseDto;
import com.team6.sole.domain.follow.entity.Follow;
import com.team6.sole.domain.member.MemberRepository;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;
    
    // 팔로잉 보기
    public List<FollowResponseDto> showFollows(String socialId) {
        List<Follow> follows = followRepository.findByFromMember_SocialId(socialId);

        return follows.stream()
                .map(FollowResponseDto::of)
                .collect(Collectors.toList());
    }

    // 팔로우
    @Transactional
    public String toFollow(String socialId, Long toMemberId) {
        Member fromMember = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.MEMBER_NOT_FOUND));

        Member toMember = memberRepository.findById(toMemberId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.MEMBER_NOT_FOUND));

        Follow follow = Follow.builder()
                .fromMember(fromMember)
                .toMember(toMember)
                .build();
        followRepository.save(follow);

        return toMember.getNickname() + "님을 팔로우했습니다...!";
    }
    
    // 팔로우 취소
    @Transactional
    public String unFollow(Long followId) {
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.FOLLOW_NOT_FOUND));

        followRepository.deleteByFollowId(followId);

        return follow.getToMember().getNickname() + "님의 팔로우를 취소했습니다...!";
    }
}
