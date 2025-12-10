package com.twog.shopping.domain.promotion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twog.shopping.domain.log.entity.History;
import com.twog.shopping.domain.log.entity.HistoryActionType;
import com.twog.shopping.domain.log.entity.HistoryRefTable;
import com.twog.shopping.domain.log.repository.HistoryRepository;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.domain.promotion.dto.CampaignRequestDto;
import com.twog.shopping.domain.promotion.dto.CampaignResponseDto;
import com.twog.shopping.domain.promotion.entity.*;
import com.twog.shopping.global.error.exception.PromotionException;
import com.twog.shopping.domain.promotion.repository.*;
import com.twog.shopping.global.error.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.twog.shopping.domain.promotion.dto.CampaignDetailResponseDto;
import com.twog.shopping.domain.promotion.dto.MessageSendLogResponseDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromotionService {

    private final CampaignRepository campaignRepository;
    private final CampaignTargetSegmentRepository campaignTargetSegmentRepository;
    private final SegmentRepository segmentRepository;
    private final MessageSendLogRepository messageSendLogRepository;
    private final HistoryRepository historyRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    // 캠페인 생성
    @Transactional
    public CampaignResponseDto createCampaign(CampaignRequestDto requestDto) {
        Campaign campaign = Campaign.builder()
                .campaignName(requestDto.getCampaignName())
                .campaignContent(requestDto.getCampaignContent())
                .campaignScheduled(requestDto.getCampaignScheduled())
                .campaignStatus(CampaignStatus.SCHEDULED)
                .build();
        Campaign savedCampaign = campaignRepository.save(campaign);

        // 타겟 세그먼트 설정
        if (requestDto.getTargetSegmentIds() != null) {
            addTargetSegments(savedCampaign, requestDto.getTargetSegmentIds());
        }

        return new CampaignResponseDto(savedCampaign);
    }

    // 캠페인 수정
    @Transactional
    public CampaignResponseDto updateCampaign(Long campaignId, CampaignRequestDto requestDto) {
        Campaign campaign = findCampaignById(campaignId);
        campaign.updateInfo(requestDto.getCampaignName(), requestDto.getCampaignContent(),
                requestDto.getCampaignScheduled());

        // 세그먼트 재설정 (기존 삭제 후 추가)
        if (requestDto.getTargetSegmentIds() != null) {
            campaignTargetSegmentRepository.deleteByCampaign_CampaignId(campaignId);
            addTargetSegments(campaign, requestDto.getTargetSegmentIds());
        }

        return new CampaignResponseDto(campaign);
    }

    // 논리적 삭제
    @Transactional
    public void deleteCampaign(Long campaignId) {
        Campaign campaign = findCampaignById(campaignId);
        campaign.deleteLogical();
    }

    // 타겟 세그먼트 추가 로직
    private void addTargetSegments(Campaign campaign, List<Long> segmentIds) {
        List<Segment> segments = segmentRepository.findAllById(segmentIds);
        for (Segment segment : segments) {
            CampaignTargetSegment targetSegment = CampaignTargetSegment.builder()
                    .campaign(campaign)
                    .segment(segment)
                    .build();
            campaignTargetSegmentRepository.save(targetSegment);
        }
    }

    // 배치 발송 시뮬레이션
    @Transactional
    public void executeCampaign(Long campaignId) {
        Campaign campaign = findCampaignById(campaignId);

        // 1. 상태 검증 (RUNNING 이어야 함)
        if (campaign.getCampaignStatus() != CampaignStatus.RUNNING) {
            throw new PromotionException("캠페인이 실행 중(RUNNING) 상태가 아닙니다.");
        }

        // 2. 시간 검증 (예약 시간이 현재 시간보다 이전이어야 함)
        if (campaign.getCampaignScheduled().isAfter(LocalDateTime.now())) {
            throw new PromotionException("캠페인 예약 시간이 아직 되지 않았습니다.");
        }

        // 3. 타겟 유저 조회 및 페이징 처리 (Chunk Size 100)
        // 실제로는 SegmentRule을 해석해야 하지만, 여기서는 전체 유저를 대상으로 한다고 가정하거나
        // 세그먼트에 포함된 유저를 가져오는 로직이 필요함.
        // 현재는 Member 전체를 100명씩 끊어서 처리하는 예시로 구현.

        int chunkSize = 100;
        int pageNumber = 0;
        Page<Member> memberPage;

        do {
            Pageable pageable = PageRequest.of(pageNumber, chunkSize);
            memberPage = memberRepository.findAll(pageable); // 실제로는 세그먼트 조건에 맞는 유저 조회 필요

            List<MessageSendLog> logsToSave = new ArrayList<>();
            List<String> historyDetails = new ArrayList<>();

            for (Member member : memberPage.getContent()) {
                // MessageSendLog 생성
                logsToSave.add(MessageSendLog.builder()
                        .campaign(campaign)
                        .member(member)
                        .build());

                historyDetails.add("Sent to member: " + member.getMemberId());
            }

            // 로그 저장 (Bulk Insert 효과)
            messageSendLogRepository.saveAll(logsToSave);

            Map<String, Object> detail = new LinkedHashMap<>();
            detail.put("event", "CAMPAIGN_BATCH_EXECUTED");
            detail.put("campaign_id", campaign.getCampaignId());
            detail.put("campaign_name", campaign.getCampaignName());
            detail.put("batch_size", logsToSave.size()); // 이번 페이지에서 보낸 유저 수
            detail.put("sent_member_ids",
                    memberPage.getContent().stream()
                            .map(Member::getMemberId)
                            .toList());

            String detailJson;
            try {
                detailJson = objectMapper.writeValueAsString(detail);
            } catch (JsonProcessingException e) {
                // 문제가 생겨도 최소 텍스트는 남기고 싶다면
                detailJson = String.format(
                        "{\"event\":\"CAMPAIGN_BATCH_EXECUTED\",\"campaign_id\":%d,\"error\":\"json_error\"}",
                        campaign.getCampaignId());
            }

            // 통합 히스토리 저장
            History history = History.create(
                    HistoryActionType.SYSTEM_UPDATE, // actionType
                    0L, // memberId (SYSTEM)
                    detailJson, // JSON detail
                    "SYSTEM_BATCH", // ipAddress
                    HistoryRefTable.campaign, // refTable
                    campaign.getCampaignId(), // refId (연결 PK)
                    "SYSTEM_BATCH" // userAgent
            );

            historyRepository.save(history);

            pageNumber++;
        } while (memberPage.hasNext());

        // 배치 완료 후 상태 변경
        campaign.end();
    }

    // 이메일 클릭 추적
    @Transactional
    public void trackEmailClick(Long sendId) {
        MessageSendLog log = messageSendLogRepository.findById(sendId)
                .orElseThrow(() -> new ResourceNotFoundException("발송 이력을 찾을 수 없습니다."));
        log.markAsClicked();
    }

    // 캠페인 목록 페이징 조회
    @Transactional(readOnly = true)
    public Page<CampaignResponseDto> getCampaignsPage(Pageable pageable) {
        return campaignRepository.findAll(pageable)
                .map(CampaignResponseDto::new);
    }

    // 캠페인 상세 조회 (통계 포함)
    @Transactional(readOnly = true)
    public CampaignDetailResponseDto getCampaignDetail(Long campaignId) {
        Campaign campaign = findCampaignById(campaignId);

        // 통계 쿼리 (count)
        long totalSentCount = messageSendLogRepository.countByCampaign_CampaignId(campaignId);
        long totalClickedCount = messageSendLogRepository.countByCampaign_CampaignIdAndSendClickedIsNotNull(campaignId);

        return new CampaignDetailResponseDto(campaign, totalSentCount, totalClickedCount);
    }

    // 캠페인 발송 이력 조회
    @Transactional(readOnly = true)
    public Page<MessageSendLogResponseDto> getCampaignSendLogs(Long campaignId, Pageable pageable) {
        // 캠페인 존재 확인
        findCampaignById(campaignId);

        return messageSendLogRepository.findByCampaign_CampaignId(campaignId, pageable)
                .map(MessageSendLogResponseDto::new);
    }

    private Campaign findCampaignById(Long campaignId) {
        return campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResourceNotFoundException("캠페인을 찾을 수 없습니다."));
    }
}
