package com.team6.sole.domain.follow;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<Member> followingMembers = showFollowingMembers(member.getSocialId());
        List<Course> followingsCourses = courseRepository.findByWriterIn(followingMembers);

        return followingCoursesToDto(member, followingsCourses);
    }

    // Entity To Dto
    public List<FollowDetailResponseDto> followingCoursesToDto(Member member, List<Course> followingsCourses) {
        return followingsCourses.stream()
                .map(course -> FollowDetailResponseDto.of(
                        course.getWriter(),
                        isCourseScrap(member, course.getCourseId()),
                        course))
                .collect(Collectors.toList());
    }

    // 팔로잉하는 사람들 보기
    public List<Member> showFollowingMembers(String socialId) {
        return followRepository.findByFromMember_SocialId(socialId).stream()
                .map(Follow::getToMember)
                .collect(Collectors.toList());
    }

    // 팔로잉한 사람들의 코스들 중 스크랩한 코스(T/F)
    public Boolean isCourseScrap(Member member, Long courseId) {
        return courseMemberRepository.existsByMemberAndCourse_CourseId(member, courseId);
    }

    // 팔로잉 보기
    @Transactional(readOnly = true)
    public List<FollowResponseDto> showFollowings(Member member) {
        List<Follow> followingMembers = followRepository.findByFromMember_SocialId(member.getSocialId());

        return FollowResponseDto.of(followingMembers);
    }

    // 팔로워 보기
    @Transactional(readOnly = true)
    public List<FollowResponseDto> showFollowers(Member member) {
        List<Follow> followers = followRepository.findByToMember_SocialId(member.getSocialId());

        return followerCoursesToDto(member, followers);
    }

    // Entity To Dto
    public List<FollowResponseDto> followerCoursesToDto(Member member, List<Follow> followers) {
        return followers.stream()
                .map(follower -> FollowResponseDto.of(
                        follower,
                        isFollow(member, follower)))
                .collect(Collectors.toList());
    }

    // 팔로워들의 팔로잉 여부
    public FollowStatus isFollow(Member member, Follow follower) {
        return followRepository.existsByFromMember_SocialIdAndToMember_SocialId(member.getSocialId(), follower.getFromMember().getSocialId())
                ? FollowStatus.FOLLOWING
                : FollowStatus.NOT_FOLLOW;
    }

    // 팔로잉 상세정보
    @Transactional(readOnly = true)
    public FollowInfoResponseDto showFollowInfo(Member member, String followInfoId, Long courseId) {
        Member followInfoMember = memberRepository.findBySocialId(followInfoId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.MEMBER_NOT_FOUND));

        // 인기 코스 set
        FollowInfoResponseDto followInfoResponseDto = FollowInfoResponseDto.of(followInfoMember);
        followInfoResponseDto.setPopularCourse(isPopularCourse(followInfoMember));

        // 최근 코스들 set
        List<Course> courses = courseCustomRepository.findAllByWriter(courseId, followInfoMember);
        followInfoResponseDto.setRecentCourses(showRecentCourses(courses, member, followInfoMember));

        return followInfoResponseDto;
    }

    // 최근 코스 모아보기
    public List<HomeResponseDto> showRecentCourses(List<Course> courses, Member member, Member followInfoMember) {
        return courses.stream()
                .map(recent -> HomeResponseDto.of(
                        recent, 
                        isCourseScrap(member, recent.getCourseId()), 
                        isFinalPage(courses, followInfoMember)))
                .collect(Collectors.toList());
    }

    // 마지막 페이지 판별(TODO -> PageDTO 클래스 따로파서 static method로)
    public Boolean isFinalPage(List<Course> courses, Member followInfoMember) {
        return courses.size() - 1 != -1 
                && courseCustomRepository.findAllByWriter(
                        courses.get(courses.size() - 1).getCourseId(),
                        followInfoMember).isEmpty();
    }

    // 인기 코스 존재여부 판별
    public HomeResponseDto isPopularCourse(Member followInfoMember) {
        return followInfoMember.getCourses().isEmpty()
                ? null
                : showPopularCourse(followInfoMember);
    }

    // 스크랩수 가장 높은 코스 보기
    public HomeResponseDto showPopularCourse(Member followInfoMember) {
        return showRecentCoursesSortByScrapCount(followInfoMember).stream()
                .map(popular -> HomeResponseDto.of(popular, isCourseScrap(followInfoMember, popular.getCourseId()), false))
                .collect(Collectors.toList()).get(0);
    }

    // 최근 코스 모아보기(스크랩수 내림차순)
    public List<Course> showRecentCoursesSortByScrapCount(Member followInfoMember) {
        return courseRepository.findAllByWriter(
                followInfoMember,
                Sort.by(Sort.Order.desc("scrapCount"),
                        Sort.Order.desc("createdAt")));
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
            removeFollowCount(checkFollow.get());
        } else {
            Follow follow = FollowResponseDto.followToEntity(fromMember, toMember);
            followRepository.save(follow);

            addFollowCount(fromMember, toMember);
            publishFollowNotification(fromMember, toMember);

            return toMember.getNickname() + "님을 팔로우했습니다...!";
        }

        return checkFollow.get().getToMember().getNickname() + "님의 팔로우를 취소했습니다...!";
    }

    // 언팔로우(팔로워 - 1, 팔로잉 - 1)
    public void removeFollowCount(Follow follow) {
        follow.getFromMember().getFollowInfo().removeFollowing(); /*DirtyChecking*/
        follow.getToMember().getFollowInfo().removeFollower(); /*DirtyChecking*/

        followRepository.deleteByFromMember_MemberIdAndToMember_MemberId(follow.getFromMember().getMemberId(), follow.getToMember().getMemberId());
    }

    // 팔로우(팔로워 + 1, 팔로잉 + 1)
    public void addFollowCount(Member fromMember, Member toMember) {
        fromMember.getFollowInfo().addFollowing(); /*DirtyChecking*/
        toMember.getFollowInfo().addFollower(); /*DirtyChecking*/
    }

    // 팔로우 알림 이벤트 전송
    public void publishFollowNotification(Member fromMember, Member toMember) {
        try {
            applicationEventPublisher.publishEvent(new FollowEvent(fromMember, toMember));
        } catch (Exception e) {
            log.error("푸시 알림 전송에 실패했습니다 - {}", e.getMessage());
        }
    }
}
