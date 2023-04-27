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
import com.team6.sole.domain.home.repository.CourseMemberRepository;
import com.team6.sole.domain.home.repository.CourseRepository;
import com.team6.sole.domain.member.MemberRepository;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FollowService {
    private final CourseMemberRepository courseMemberRepository;
    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;
    private final CourseRepository courseRepository;
    private final CourseCustomRepository courseCustomRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    
    // 팔로잉하는 사람들 작성한 코스 보기
    @Transactional(readOnly = true)
    public List<FollowDetailResponseDto> showFollowingCourses(Member member) {
        List<Member> followings = followRepository.findByFromMember_SocialId(member.getSocialId()).stream()
                .map(Follow::getToMember)
                .collect(Collectors.toList());

        List<Course> followingsCourses = courseRepository.findByWriterIn(followings);

        return followingsCourses.stream()
                .map(course -> FollowDetailResponseDto.of(
                        course.getWriter(),
                        courseMemberRepository.existsByMemberAndCourse_CourseId(
                                member,
                                course.getCourseId()
                        ),
                        course))
                .collect(Collectors.toList());
    }

    // 팔로잉 보기
    @Transactional(readOnly = true)
    public List<FollowResponseDto> showFollowings(Member member) {
        List<Follow> followings = followRepository.findByFromMember_SocialId(member.getSocialId());

        return followings.stream()
                .map(FollowResponseDto::ofFollowing)
                .collect(Collectors.toList());
    }

    // 팔로워 보기
    @Transactional(readOnly = true)
    public List<FollowResponseDto> showFollowers(Member member) {
        List<Follow> followers = followRepository.findByToMember_SocialId(member.getSocialId());

        return followers.stream()
                .map(follower -> FollowResponseDto.ofFollower(
                        follower,
                        followRepository.existsByFromMember_SocialIdAndToMember_SocialId(follower.getFromMember().getSocialId(), member.getSocialId())
                ? FollowStatus.FOLLOWING : FollowStatus.NOT_FOLLOW))
                .collect(Collectors.toList());
    }

    // 팔로잉 상세정보
    @Transactional(readOnly = true)
    public FollowInfoResponseDto showFollowInfo(Member member, String followInfoId, Long courseId) {
        Member followInfoMember = memberRepository.findBySocialId(followInfoId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.MEMBER_NOT_FOUND));

        FollowInfoResponseDto followInfoResponseDto = FollowInfoResponseDto.of(followInfoMember);

        // 인기 코스 set
        followInfoResponseDto.setPopularCourse(
                followInfoMember.getCourses().isEmpty()
                        ? null
                        : courseRepository.findAllByWriter(
                                followInfoMember,
                                Sort.by(Sort.Order.desc("scrapCount"),
                                        Sort.Order.desc("createdAt"))).stream()
                        .map(popular -> HomeResponseDto.of(
                                popular,
                                courseMemberRepository.existsByMemberAndCourse_CourseId(member, popular.getCourseId()),
                                true))
                        .collect(Collectors.toList())
                        .get(0));

        // 최근 코스들 set
        List<Course> courses = courseCustomRepository.findAllByWriter(courseId, followInfoMember);
        boolean finalPage = courses.size() - 1 != -1 && courseCustomRepository.findAllByWriter(
                courses.get(courses.size() - 1).getCourseId(),
                followInfoMember).isEmpty();
        followInfoResponseDto.setRecentCourses(courses.stream()
                .map(recent -> HomeResponseDto.of(
                        recent,
                        courseMemberRepository.existsByMemberAndCourse_CourseId(member, recent.getCourseId()),
                        finalPage))
                .collect(Collectors.toList()));
        return followInfoResponseDto;
    }

    // 팔로우 및 언팔로우
    @Transactional
    public String toFollow(Member fromMember, Long toMemberId) {
        // 발신자
        Member toMember = memberRepository.findById(toMemberId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.MEMBER_NOT_FOUND));

        Optional<Follow> checkFollow = followRepository
                .findByFromMember_MemberIdAndToMember_MemberId(fromMember.getMemberId(), toMember.getMemberId());

        if (checkFollow.isPresent()) {
            checkFollow.get().getFromMember().getFollowInfo().removeFollowing(); /*DirtyChecking*/
            checkFollow.get().getToMember().getFollowInfo().removeFollower(); /*DirtyChecking*/

            followRepository.deleteByFromMember_MemberIdAndToMember_MemberId(fromMember.getMemberId(), toMember.getMemberId());
        } else {
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

        return checkFollow.get().getToMember().getNickname() + "님의 팔로우를 취소했습니다...!";
    }
}
