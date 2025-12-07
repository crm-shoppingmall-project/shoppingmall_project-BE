package com.twog.shopping.domain.log.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twog.shopping.domain.log.entity.History;
import com.twog.shopping.domain.log.entity.HistoryActionType;
import com.twog.shopping.domain.log.repository.HistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    public void saveHistory(
            Long memberId,
            HistoryActionType actionType,
            Map<String,Object> detail,
            HttpServletRequest request
    ) {

        try {
            String detailJson = (detail == null || detail.isEmpty())
                    ? "{}"
                    : objectMapper.writeValueAsString(detail);

            History history = History.builder()
                    .memberId(memberId)
                    .actionType(actionType)
                    .actionCategory(actionType.getCategory())
                    .datetime(LocalDateTime.now())
                    .detailJson(detailJson)
                    .ipAddress(getClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .refTable(null)
                    .refId(null)
                    .build();

            historyRepository.save(history);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 처리 중 오류가 발생했습니다.",e);
        }
    }

    public void logAuth(Long memberId, HistoryActionType type, Map<String,Object> detail, HttpServletRequest request){

        saveHistory(memberId,type,detail,request);
    }


    public void logPurchaseCompleted(Long memberId, HistoryActionType type, Long purchase_detail_id, Integer purchase_paid_amount, HttpServletRequest request){
        Map<String, Object> detail = Map.of(
                "purchase_detail_id",purchase_detail_id,
                "purchase_paid_amount",purchase_paid_amount

        );

        saveHistory(memberId,HistoryActionType.PURCHASE_COMPLETED,detail,request);
    }



    public void logview(Long memberId, HistoryActionType type, Long product_id, HttpServletRequest request){

        Map<String, Object> detail = Map.of(
                "product_id",product_id
        );

        saveHistory(memberId,type,detail,request);
    }


    private String getClientIp(HttpServletRequest request){

        String forwarded = request.getHeader("X-Forwarded-For");
        if(forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }


}
