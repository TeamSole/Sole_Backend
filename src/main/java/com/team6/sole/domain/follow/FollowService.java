package com.team6.sole.domain.follow;

import com.team6.sole.domain.follow.dto.FollowDetailResponseDto;
import com.team6.sole.domain.follow.dto.FollowInfoResponseDto;
import com.team6.sole.domain.follow.dto.FollowResponseDto;
import com.team6.sole.domain.follow.entity.Follow;
import com.team6.sole.domain.follow.event.FollowEvent;
import com.team6.sole.domain.follow.model.FollowStatus;
import com.team6.sole.domain.home.dto.HomeResponseDto;
import com.team6.sole.domain.home.entity.Course;
import com.team6.sole.domain.home.repository.CourseCustomRepository;
import com.team6.sole.domain.home.repository.CourseRepository;
import com.team6.sole.domain.member.MemberRepository;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;
    private final CourseRepository courseRepository;
    private final CourseCustomRepository courseCustomRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    
    // 팔로잉하는 사람들 작성한 코스 보기
    @Transactional(readOnly = true)
    public List<FollowDetailResponseDto> showFollwingCourses(String socialId) {
        List<Member> followings = followRepository.findByFromMember_SocialId(socialId).stream()
                .map(Follow::getToMember)
                .collect(Collectors.toList());

        List<Course> followingsCourses = courseRepository.findByWriterIn(followings);

        return followingsCourses.stream()
                .map(course -> FollowDetailResponseDto.of(
                        course.getWriter(),
                        followRepository.existsByFromMember_SocialIdAndToMember_SocialId(socialId, course.getWriter().getSocialId()),
                        course))
                .collect(Collectors.toList());
    }

    // 팔로잉 보기
    @Transactional(readOnly = true)
    public List<FollowResponseDto> showFollowings(String socialId) {
        List<Follow> followings = followRepository.findByFromMember_SocialId(socialId);

        return followings.stream()
                .map(FollowResponseDto::ofFollowing)
                .collect(Collectors.toList());
    }

    // 팔로워 보기
    @Transactional(readOnly = true)
    public List<FollowResponseDto> showFollowers(String socialId) {
        List<Follow> followers = followRepository.findByToMember_SocialId(socialId);

        return followers.stream()
                .map(follower -> FollowResponseDto.ofFollower(
                        follower,
                        followRepository.existsByFromMember_SocialIdAndToMember_SocialId(follower.getFromMember().getSocialId(), socialId)
                ? FollowStatus.FOLLOWING : FollowStatus.NOT_FOLLOW))
                .collect(Collectors.toList());
    }

    // 팔로잉 상세정보
    @Transactional(readOnly = true)
    public FollowInfoResponseDto showFollowInfo(String socialId, String followInfoId, Long courseId) {
        Member followInfoMember = memberRepository.findBySocialId(followInfoId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.MEMBER_NOT_FOUND));

        FollowInfoResponseDto followInfoResponseDto = FollowInfoResponseDto.of(followInfoMember);
        // 인기 코스 set
        followInfoResponseDto.setPopularCourse((HomeResponseDto) followInfoMember.getCourses().stream()
                .map(popular -> HomeResponseDto.of(popular, followRepository.existsByFromMember_SocialIdAndToMember_SocialId(socialId, followInfoMember.getSocialId())))
                .limit(1));
        // 최근 코스들 set
        followInfoResponseDto.setRecentCourses(courseCustomRepository.findAllByWriter(courseId, followInfoMember).stream()
                .map(recent -> HomeResponseDto.of(
                        recent,
                        followRepository.existsByFromMember_SocialIdAndToMember_SocialId(socialId, followInfoMember.getSocialId())))
                .collect(Collectors.toList()));

        return followInfoResponseDto;
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

        fromMember.getFollowInfo().addFollowing(); /*DirtyChecking*/
        toMember.getFollowInfo().addFollower(); /*DirtyChecking*/

        //알림 이벤트 전송(fcm)
        try {
            applicationEventPublisher.publishEvent(new FollowEvent(fromMember, toMember));
        } catch (Exception e) {
            log.error("푸시 알림 전송에 실패했습니다 - {}", e.getMessage());
        }

        return toMember.getNickname() + "님을 팔로우했습니다...!";
    }
    
    // 팔로우 취소
    @Transactional
    public String unFollow(Long followId) {
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.FOLLOW_NOT_FOUND));

        follow.getFromMember().getFollowInfo().removeFollowing(); /*DirtyChecking*/
        follow.getToMember().getFollowInfo().removeFollower(); /*DirtyChecking*/

        followRepository.deleteByFollowId(followId);

        return follow.getToMember().getNickname() + "님의 팔로우를 취소했습니다...!";
    }
}
