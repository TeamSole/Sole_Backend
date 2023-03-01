package com.team6.sole.domain.notice.event;

import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.notice.dto.NoticeRequestDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class NoticeEvent {
    private final List<Member> receivers;

    private final NoticeRequestDto noticeRequestDto;
}
