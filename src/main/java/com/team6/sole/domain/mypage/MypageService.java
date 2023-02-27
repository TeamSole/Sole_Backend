package com.team6.sole.domain.mypage;

import com.team6.sole.domain.member.MemberRepository;
import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.mypage.dto.MypageRequestDto;
import com.team6.sole.domain.mypage.dto.MypageResponseDto;
import com.team6.sole.global.config.s3.AwsS3ServiceImpl;
import com.team6.sole.global.error.ErrorCode;
import com.team6.sole.global.error.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MypageService {
    private final MemberRepository memberRepository;
    private final AwsS3ServiceImpl awsS3Service;

    // 마이페이지 조회
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

    // 탈퇴
    @Transactional
    public String delMember(String socialId) {
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new BadRequestException(ErrorCode.MEMBER_NOT_FOUND));

        memberRepository.deleteByMemberId(member.getMemberId());

        return "탈퇴가 완료되었습니다...!";
    }
}
