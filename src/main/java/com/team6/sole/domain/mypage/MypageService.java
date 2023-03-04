package com.team6.sole.domain.mypage;

import com.team6.sole.domain.member.MemberRepository;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.mypage.dto.*;
import com.team6.sole.global.config.s3.AwsS3ServiceImpl;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.BadRequestException;
import com.team6.sole.global.error.exception.NotFoundException;
import com.team6.sole.infra.notification.NotificationRepository;
import com.team6.sole.infra.notification.entity.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MypageService {
    private final MemberRepository memberRepository;
    private final AwsS3ServiceImpl awsS3Service;
    private final NotificationRepository notificationRepository;

    // 마이페이지 조회(홈 포함)
    @Transactional(readOnly = true)
    public MypageResponseDto showMypage(String socialId) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.MEMBER_NOT_FOUND));
        
        return MypageResponseDto.of(member);
    }
    
    // 마이페이지 수정
    @Transactional
    public MypageResponseDto modMypage(String socialId, MultipartFile multipartFile, MypageRequestDto mypageRequestDto) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.MEMBER_NOT_FOUND));

        member.modMypage(
                multipartFile == null
                        ? null
                        : awsS3Service.uploadImage(multipartFile, "member"),
                mypageRequestDto.getNickname(),
                mypageRequestDto.getDescription()
        ); /*Dirty Checking으로 save 없이 update 쿼리 실행..!*/

        return MypageResponseDto.of(member);
    }

    // 알림 설정 조회
    @Transactional(readOnly = true)
    public NotSettingReseponseDto showNotSetting(String socialId) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return NotSettingReseponseDto.of(member);
    }
    
    // 알림 설정
    @Transactional
    public NotSettingReseponseDto modNotSetting(String socialId, NotSettingRequestDto notSettingRequestDto) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        member.modNotSetting(notSettingRequestDto.isActivityNot(),
                notSettingRequestDto.isMarketingNot());

        return NotSettingReseponseDto.of(member);
    }
    
    // 알림 히스토리 조회
    @Transactional(readOnly = true)
    public List<NotHistoryResponseDto> showNotHistories(String socialId) {
        Member receiver = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        List<Notification> notifications = notificationRepository
                .findAllByReceiver(receiver, Sort.by(Sort.Direction.DESC, "createdAt"));

        return notifications.stream()
                .map(NotHistoryResponseDto::of)
                .collect(Collectors.toList());
    }
    
    // 탈퇴
    @Transactional
    public String delMember(String socialId) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.MEMBER_NOT_FOUND));

        memberRepository.deleteByMemberId(member.getMemberId());

        return "탈퇴가 완료되었습니다...!";
    }
}
