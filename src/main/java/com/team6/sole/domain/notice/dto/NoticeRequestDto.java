package com.team6.sole.domain.notice.dto;

import com.team6.sole.domain.member.entity.Member;
import com.team6.sole.domain.notice.entity.Notice;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeRequestDto {
    @ApiModelProperty(value = "공지사항 제목", example = "쏠 오픈!!")
    private String title;

    @ApiModelProperty(value = "공지사항 내용", example = "우리가 드디어 오픈했어요!!")
    private String content;

    public static Notice noticeToEntity(Member writer, NoticeRequestDto noticeRequestDto) {
        return Notice.builder()
            .title(noticeRequestDto.getTitle())
            .content(noticeRequestDto.getContent())
            .writer(writer)
        .build();
    }
}
