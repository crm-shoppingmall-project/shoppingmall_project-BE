package com.twog.shopping.domain.promotion.service;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.MemberGrade;
import com.twog.shopping.domain.member.entity.MemberStatus;
import com.twog.shopping.domain.member.entity.UserRole;
import com.twog.shopping.domain.member.repository.MemberGradeRepository;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.domain.promotion.dto.CampaignRequestDto;
import com.twog.shopping.domain.promotion.dto.CampaignResponseDto;
import com.twog.shopping.domain.promotion.dto.CampaignDetailResponseDto;
import com.twog.shopping.domain.promotion.dto.MessageSendLogResponseDto;
import com.twog.shopping.domain.promotion.entity.Campaign;
import com.twog.shopping.domain.promotion.entity.CampaignStatus;
import com.twog.shopping.domain.promotion.entity.MessageSendLog;
import com.twog.shopping.domain.promotion.repository.CampaignRepository;
import com.twog.shopping.domain.promotion.repository.MessageSendLogRepository;
import com.twog.shopping.global.common.entity.GradeName;
import com.twog.shopping.global.error.exception.PromotionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest; // PageRequest import 추가
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
class PromotionServiceTest {

        @Autowired
        private PromotionService promotionService;

        @Autowired
        private CampaignRepository campaignRepository;

        @Autowired
        private MessageSendLogRepository messageSendLogRepository;

        @Autowired
        private MemberRepository memberRepository;

        @Autowired
        private MemberGradeRepository memberGradeRepository;

        private Long memberId;

        @BeforeEach
        void setUp() {
                MemberGrade bronzeGrade = memberGradeRepository.save(MemberGrade.builder()
                                .gradeName(GradeName.BRONZE)
                                .gradeDesc("브론즈 등급")
                                .build());

                Member member = Member.builder()
                                .memberName("promoUser")
                                .memberEmail("promo@test.com")
                                .memberPwd("password")
                                .memberBirth(LocalDate.of(1990, 1, 1))
                                .memberGender('M')
                                .memberPhone("010-1234-5678")
                                .memberGrade(bronzeGrade)
                                .memberRole(UserRole.USER)
                                .memberStatus(MemberStatus.active)
                                .memberCreated(LocalDateTime.now())
                                .memberUpdated(LocalDateTime.now())
                                .memberLastAt(LocalDateTime.now())
                                .build();
                Member savedMember = memberRepository.save(member);
                memberId = savedMember.getMemberId();
        }

        @Test
        @DisplayName("캠페인을 정상적으로 생성한다")
        void createCampaignTest() {
                // given
                CampaignRequestDto requestDto = CampaignRequestDto.builder()
                                .campaignName("Test Campaign")
                                .campaignContent("Test Content")
                                .campaignScheduled(LocalDateTime.now().plusDays(1))
                                .build();

                // when
                CampaignResponseDto responseDto = promotionService.createCampaign(requestDto);

                // then
                assertThat(responseDto.getCampaignName()).isEqualTo("Test Campaign");
                assertThat(responseDto.getCampaignStatus()).isEqualTo(CampaignStatus.SCHEDULED);

                Campaign savedCampaign = campaignRepository.findById(responseDto.getCampaignId()).orElse(null);
                assertThat(savedCampaign).isNotNull();
                assertThat(savedCampaign.getCampaignName()).isEqualTo("Test Campaign");
        }

        @Test
        @DisplayName("캠페인 정보를 수정한다")
        void updateCampaignTest() {
                // given
                Campaign campaign = campaignRepository.save(Campaign.builder()
                                .campaignName("Original Name")
                                .campaignContent("Original Content")
                                .campaignScheduled(LocalDateTime.now().plusDays(1))
                                .campaignStatus(CampaignStatus.SCHEDULED)
                                .build());

                CampaignRequestDto updateDto = CampaignRequestDto.builder()
                                .campaignName("Updated Name")
                                .campaignContent("Updated Content")
                                .campaignScheduled(LocalDateTime.now().plusDays(2))
                                .build();

                // when
                CampaignResponseDto responseDto = promotionService.updateCampaign(campaign.getCampaignId(), updateDto);

                // then
                assertThat(responseDto.getCampaignName()).isEqualTo("Updated Name");

                Campaign updatedCampaign = campaignRepository.findById(campaign.getCampaignId()).orElseThrow();
                assertThat(updatedCampaign.getCampaignContent()).isEqualTo("Updated Content");
        }

        @Test
        @DisplayName("캠페인을 논리적으로 삭제한다")
        void deleteCampaignTest() {
                // given
                Campaign campaign = campaignRepository.save(Campaign.builder()
                                .campaignName("To Delete")
                                .campaignContent("Content")
                                .campaignScheduled(LocalDateTime.now().plusDays(1))
                                .campaignStatus(CampaignStatus.SCHEDULED)
                                .build());

                // when
                promotionService.deleteCampaign(campaign.getCampaignId());

                // then
                Campaign deletedCampaign = campaignRepository.findById(campaign.getCampaignId()).orElseThrow();
                assertThat(deletedCampaign.getCampaignStatus()).isEqualTo(CampaignStatus.ENDED);
        }

        @Test
        @DisplayName("캠페인 실행 시 조건(RUNNING 상태, 시간)을 만족하면 메일 발송 로그를 생성한다")
        void executeCampaignSuccessTest() {
                // given
                // 시간 조건을 만족시키기 위해 과거 시간으로 설정
                Campaign campaign = campaignRepository.save(Campaign.builder()
                                .campaignName("Running Campaign")
                                .campaignContent("Content")
                                .campaignScheduled(LocalDateTime.now().minusHours(1))
                                .campaignStatus(CampaignStatus.RUNNING)
                                .build());

                // when
                promotionService.executeCampaign(campaign.getCampaignId());

                // then
                List<MessageSendLog> logs = messageSendLogRepository.findAll();
                // setUp에서 1명 생성 + executeCampaign이 전체 멤버 대상으로 발송하므로 최소 1개 이상 존재해야 함.
                assertThat(logs).isNotEmpty();

                // 방금 생성한 캠페인에 대한 로그인지 확인
                boolean hasLogForCampaign = logs.stream()
                                .anyMatch(log -> log.getCampaign().getCampaignId().equals(campaign.getCampaignId()));
                assertThat(hasLogForCampaign).isTrue();

                // 캠페인 상태가 ENDED로 변경되었는지 확인
                Campaign finishedCampaign = campaignRepository.findById(campaign.getCampaignId()).orElseThrow();
                assertThat(finishedCampaign.getCampaignStatus()).isEqualTo(CampaignStatus.ENDED);
        }

        @Test
        @DisplayName("캠페인이 실행 중(RUNNING) 상태가 아니면 예외를 던진다")
        void executeCampaignFailStatusTest() {
                // given
                Campaign campaign = campaignRepository.save(Campaign.builder()
                                .campaignName("Scheduled Campaign")
                                .campaignContent("Content")
                                .campaignScheduled(LocalDateTime.now().minusHours(1))
                                .campaignStatus(CampaignStatus.SCHEDULED) // Not RUNNING
                                .build());

                // when & then
                assertThrows(PromotionException.class,
                                () -> promotionService.executeCampaign(campaign.getCampaignId()));
        }

        @Test
        @DisplayName("캠페인 예약 시간이 아직 되지 않았으면 예외를 던진다")
        void executeCampaignFailTimeTest() {
                // given
                Campaign campaign = campaignRepository.save(Campaign.builder()
                                .campaignName("Future Campaign")
                                .campaignContent("Content")
                                .campaignScheduled(LocalDateTime.now().plusHours(1)) // Future
                                .campaignStatus(CampaignStatus.RUNNING)
                                .build());

                // when & then
                assertThrows(PromotionException.class,
                                () -> promotionService.executeCampaign(campaign.getCampaignId()));
        }

        @Test
        @DisplayName("이메일 클릭 시 로그의 클릭 시간을 업데이트한다")
        void trackEmailClickTest() {
                // given
                Campaign campaign = campaignRepository.save(Campaign.builder()
                                .campaignName("Click Test Campaign")
                                .campaignContent("Content")
                                .campaignScheduled(LocalDateTime.now().minusHours(1))
                                .campaignStatus(CampaignStatus.RUNNING)
                                .build());

                Member member = memberRepository.findById(memberId).orElseThrow();

                MessageSendLog log = messageSendLogRepository.save(MessageSendLog.builder()
                                .campaign(campaign)
                                .member(member)
                                .build());

                // when
                promotionService.trackEmailClick(log.getSendId());

                // then
                MessageSendLog updatedLog = messageSendLogRepository.findById(log.getSendId()).orElseThrow();
                assertThat(updatedLog.getSendClicked()).isNotNull();
        }

        @Test
        @DisplayName("캠페인 목록을 페이징 조회한다")
        void getCampaignsPageTest() {
                // given
                for (int i = 0; i < 5; i++) {
                        campaignRepository.save(Campaign.builder()
                                        .campaignName("Paged Campaign " + i)
                                        .campaignContent("Content")
                                        .campaignScheduled(LocalDateTime.now().plusDays(1))
                                        .campaignStatus(CampaignStatus.SCHEDULED)
                                        .build());
                }

                PageRequest pageable = PageRequest.of(0, 3);

                // when
                org.springframework.data.domain.Page<CampaignResponseDto> result = promotionService
                                .getCampaignsPage(pageable);

                // then
                assertThat(result).isNotNull();
                assertThat(result.getContent()).hasSize(3);
                assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("캠페인 상세 정보를 조회하면 통계 데이터도 포함된다")
        void getCampaignDetailTest() {
                // given
                Campaign campaign = campaignRepository.save(Campaign.builder()
                                .campaignName("Detail Campaign")
                                .campaignContent("Content")
                                .campaignScheduled(LocalDateTime.now().minusHours(2))
                                .campaignStatus(CampaignStatus.RUNNING)
                                .build());

                Member member = memberRepository.findById(memberId).orElseThrow();

                // 2개의 로그 생성, 1개는 클릭됨
                MessageSendLog log1 = messageSendLogRepository.save(MessageSendLog.builder()
                                .campaign(campaign)
                                .member(member)
                                .build());
                log1.markAsClicked();
                messageSendLogRepository.save(log1);

                MessageSendLog log2 = messageSendLogRepository.save(MessageSendLog.builder()
                                .campaign(campaign)
                                .member(member)
                                .build());

                // when
                CampaignDetailResponseDto detail = promotionService.getCampaignDetail(campaign.getCampaignId());

                // then
                assertThat(detail.getCampaignName()).isEqualTo("Detail Campaign");
                assertThat(detail.getTotalSentCount()).isEqualTo(2);
                assertThat(detail.getTotalClickedCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("캠페인 발송 이력을 페이징 조회한다")
        void getCampaignSendLogsTest() {
                // given
                Campaign campaign = campaignRepository.save(Campaign.builder()
                                .campaignName("Log Campaign")
                                .campaignContent("Content")
                                .campaignScheduled(LocalDateTime.now().minusHours(2))
                                .campaignStatus(CampaignStatus.RUNNING)
                                .build());

                Member member = memberRepository.findById(memberId).orElseThrow();

                for (int i = 0; i < 5; i++) {
                        messageSendLogRepository.save(MessageSendLog.builder()
                                        .campaign(campaign)
                                        .member(member)
                                        .build());
                }

                PageRequest pageable = PageRequest.of(0, 3);

                // when
                org.springframework.data.domain.Page<MessageSendLogResponseDto> logs = promotionService
                                .getCampaignSendLogs(campaign.getCampaignId(), pageable);

                // then
                assertThat(logs).isNotNull();
                assertThat(logs.getContent()).hasSize(3);
                assertThat(logs.getTotalElements()).isEqualTo(5);
        }
}
