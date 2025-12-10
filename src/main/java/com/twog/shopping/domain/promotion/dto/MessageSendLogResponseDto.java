package com.twog.shopping.domain.promotion.dto;

import com.twog.shopping.domain.promotion.entity.MessageSendLog;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MessageSendLogResponseDto {
    private Long sendId;
    private Long memberId;
    private String memberName;
    private String memberEmail;
    private LocalDateTime sendAt;
    private LocalDateTime sendClicked;

    public MessageSendLogResponseDto(MessageSendLog log) {
        this.sendId = log.getSendId();
        this.memberId = log.getMember().getMemberId();
        this.memberName = log.getMember().getMemberName();
        this.memberEmail = log.getMember().getMemberEmail();
        this.sendAt = log.getSendAt();
        this.sendClicked = log.getSendClicked();
    }
}
