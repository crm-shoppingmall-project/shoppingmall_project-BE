package com.twog.shopping.domain.log.dto;

import com.twog.shopping.domain.log.entity.History;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HistoryResponseDTO {
    private Long historyId;
    private LocalDateTime historyDatetime;
    private String historyActionType;
    private String historyActionCategory;
    private Long historyMemberId;
    private String historyDetail;
    private String historyIpAddress;
    private String historyUserAgent;

    public static HistoryResponseDTO fromEntity(History history) {
        return HistoryResponseDTO.builder()
                .historyId(history.getHistoryId())
                .historyDatetime(history.getDatetime())
                .historyActionType(history.getHistoryActionType() != null ? history.getHistoryActionType().name() : null)
                .historyActionCategory(history.getActionCategory() != null ? history.getActionCategory().name() : null)
                .historyMemberId(history.getHistoryMemberId())
                .historyDetail(history.getHistoryDetail())
                .historyIpAddress(history.getHistoryIpAddress())
                .historyUserAgent(history.getUserAgent())
                .build();
    }
}
