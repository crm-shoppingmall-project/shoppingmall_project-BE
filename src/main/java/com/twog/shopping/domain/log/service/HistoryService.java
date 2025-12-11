package com.twog.shopping.domain.log.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twog.shopping.domain.log.dto.HistoryResponseDTO;
import com.twog.shopping.domain.log.entity.History;
import com.twog.shopping.domain.log.entity.HistoryActionType;
import com.twog.shopping.domain.log.entity.HistoryRefTable;
import com.twog.shopping.domain.log.repository.HistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final HistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    // 관리자용: 전체 로그 조회
    @Transactional(readOnly = true)
    public List<HistoryResponseDTO> getAllHistories() {
        return historyRepository.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "datetime"))
                .stream()
                .map(HistoryResponseDTO::fromEntity)
                .toList();
    }

    // 관리자용: 페이징 로그 조회
    @Transactional(readOnly = true)
    public Page<HistoryResponseDTO> getAllHistoriesPage(Pageable pageable) {
        return historyRepository.findAllOrderByDatetimeDesc(pageable)
                .map(HistoryResponseDTO::fromEntity);
    }

    public void saveHistory(
            Long memberId,
            HistoryActionType actionType,
            Map<String,Object> detail,
            HttpServletRequest request,
            HistoryRefTable refTable,
            Long refId
    ) {

        try {
            String detailJson = (detail == null || detail.isEmpty())
                    ? "{}"
                    : objectMapper.writeValueAsString(detail);

            String ip = (request == null) ? "SYSTEM" : getClientIp(request);
            String userAgent = (request == null) ? "SYSTEM_BATCH" : request.getHeader("User-Agent");

            HistoryRefTable historyRefTable = (refTable == null) ? null : refTable;
            Long historyRefTblId = (refId == null) ? null : refId;


            History history = History.builder()
                    .historyMemberId(memberId)
                    .historyActionType(actionType)
                    .actionCategory(actionType.getCategory())
                    .datetime(LocalDateTime.now())
                    .historyDetail(detailJson)
                    .historyIpAddress(ip)
                    .userAgent(userAgent)
                    .historyRefTbl(historyRefTable)
                    .historyRefTblId(historyRefTblId)
                    .build();

            historyRepository.save(history);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 처리 중 오류가 발생했습니다.",e);
        }
    }

    public void logAuth(Long memberId, HistoryActionType type, Map<String,Object> detail, HttpServletRequest request, HistoryRefTable refTable, Long refId){

        saveHistory(memberId,type,detail,request,refTable,refId);
    }


    public void logPurchaseCompleted(Long memberId, HistoryActionType type, Long purchase_detail_id, Integer purchase_paid_amount, HttpServletRequest request,HistoryRefTable refTable, Long refId){
        Map<String, Object> detail = Map.of(
                "purchase_detail_id",purchase_detail_id,
                "purchase_paid_amount",purchase_paid_amount

        );

        saveHistory(memberId,HistoryActionType.PURCHASE_COMPLETED,detail,request,refTable,refId);
    }



    public void logview(Long memberId, HistoryActionType type, Long product_id, HttpServletRequest request, HistoryRefTable refTable, Long refId){

        Map<String, Object> detail = Map.of(
                "product_id",product_id
        );

        saveHistory(memberId,type,detail,request , refTable,refId);
    }


    private String getClientIp(HttpServletRequest request){

        String forwarded = request.getHeader("X-Forwarded-For");
        if(forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }


}
