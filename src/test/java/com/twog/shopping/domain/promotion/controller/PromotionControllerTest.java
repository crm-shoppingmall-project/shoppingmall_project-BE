package com.twog.shopping.domain.promotion.controller;

import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.domain.promotion.dto.CampaignRequestDto;
import com.twog.shopping.domain.promotion.dto.CampaignResponseDto;
import com.twog.shopping.domain.promotion.entity.Campaign;
import com.twog.shopping.domain.promotion.entity.CampaignStatus;
import com.twog.shopping.domain.promotion.entity.MessageSendLog;
import com.twog.shopping.domain.promotion.repository.CampaignRepository;
import com.twog.shopping.domain.promotion.repository.MessageSendLogRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.stream.IntStream;
import com.twog.shopping.domain.promotion.dto.CampaignDetailResponseDto;
import com.twog.shopping.domain.promotion.dto.MessageSendLogResponseDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PromotionControllerTest {

        @Autowired
        private PromotionController promotionController;

        @Autowired
        private CampaignRepository campaignRepository;

        @Autowired
        private MessageSendLogRepository messageSendLogRepository;

        @Autowired
        private EntityManager entityManager;

        private Long adminMemberId;
        private Long userMemberId;
        private Long campaignId;

        @BeforeEach
        void setUp() {
                // 1. 등급 데이터 생성
                entityManager.createNativeQuery(
                                "INSERT INTO member_grade (grade_name, grade_desc) " +
                                                "SELECT 'BRONZE', '브론즈' FROM DUAL " +
                                                "WHERE NOT EXISTS (SELECT 1 FROM member_grade WHERE grade_name = 'BRONZE')")
                                .executeUpdate();

                // 2. ADMIN 회원 생성 (ID: 1)
                entityManager.createNativeQuery(
                                "INSERT INTO member (member_id, grade_code, member_name, member_gender, member_phone, member_birth, member_pwd, member_email, member_status, member_created, member_updated, member_last_at, member_role) "
                                                +
                                                "SELECT 1, (SELECT grade_code FROM member_grade WHERE grade_name = 'BRONZE' LIMIT 1), 'admin', 'M', '01000000000', '1990-01-01', 'password', 'admin@test.com', 'active', NOW(), NOW(), NOW(), 'ADMIN' "
                                                +
                                                "FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM member WHERE member_id = 1)")
                                .executeUpdate();
                adminMemberId = 1L;

                // 3. USER 회원 생성 (ID: 2)
                entityManager.createNativeQuery(
                                "INSERT INTO member (member_id, grade_code, member_name, member_gender, member_phone, member_birth, member_pwd, member_email, member_status, member_created, member_updated, member_last_at, member_role) "
                                                +
                                                "SELECT 2, (SELECT grade_code FROM member_grade WHERE grade_name = 'BRONZE' LIMIT 1), 'user', 'M', '01011112222', '1990-01-01', 'password', 'user@test.com', 'active', NOW(), NOW(), NOW(), 'USER' "
                                                +
                                                "FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM member WHERE member_id = 2)")
                                .executeUpdate();
                userMemberId = 2L;

                // 4. 테스트 캠페인 생성
                Campaign campaign = campaignRepository.save(Campaign.builder()
                                .campaignName("Test Campaign")
                                .campaignContent("Test Content")
                                .campaignScheduled(LocalDateTime.now().minusHours(1))
                                .campaignStatus(CampaignStatus.RUNNING)
                                .build());
                campaignId = campaign.getCampaignId();

                entityManager.flush();
                entityManager.clear();
        }

        // Helper: SecurityContext에 인증 정보 설정
        private void mockLogin(Long memberId) {
                Member member = entityManager.find(Member.class, memberId);
                DetailsUser detailsUser = new DetailsUser(member);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                detailsUser, null, detailsUser.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        @Test
        @DisplayName("ADMIN 권한으로 캠페인을 성공적으로 생성한다")
        void createCampaign_AsAdmin() {
                // given
                mockLogin(adminMemberId);
                CampaignRequestDto requestDto = CampaignRequestDto.builder()
                                .campaignName("New Campaign")
                                .campaignContent("Content")
                                .campaignScheduled(LocalDateTime.now().plusDays(1))
                                .build();

                // when
                ResponseEntity<CampaignResponseDto> response = promotionController.createCampaign(requestDto);

                // then
                assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getCampaignName()).isEqualTo("New Campaign");
        }

        @Test
        @DisplayName("USER 권한으로 캠페인 생성 시 AccessDeniedException이 발생한다")
        void createCampaign_AsUser_Forbidden() {
                // given
                mockLogin(userMemberId);
                CampaignRequestDto requestDto = CampaignRequestDto.builder()
                                .campaignName("Forbidden Campaign")
                                .build();

                // when & then
                assertThatThrownBy(() -> promotionController.createCampaign(requestDto))
                                .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("ADMIN 권한으로 캠페인을 성공적으로 수정한다")
        void updateCampaign_AsAdmin() {
                // given
                mockLogin(adminMemberId);
                CampaignRequestDto updateDto = CampaignRequestDto.builder()
                                .campaignName("Updated Campaign")
                                .campaignContent("Updated Content")
                                .campaignScheduled(LocalDateTime.now().plusDays(2))
                                .build();

                // when
                ResponseEntity<CampaignResponseDto> response = promotionController.updateCampaign(campaignId,
                                updateDto);

                // then
                assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
                assertThat(response.getBody().getCampaignName()).isEqualTo("Updated Campaign");
        }

        @Test
        @DisplayName("ADMIN 권한으로 캠페인을 성공적으로 삭제한다 (논리적 삭제)")
        void deleteCampaign_AsAdmin() {
                // given
                mockLogin(adminMemberId);

                // when
                ResponseEntity<Void> response = promotionController.deleteCampaign(campaignId);

                // then
                assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

                Campaign deletedCampaign = campaignRepository.findById(campaignId).orElseThrow();
                assertThat(deletedCampaign.getCampaignStatus()).isEqualTo(CampaignStatus.ENDED);
        }

        @Test
        @DisplayName("ADMIN 권한으로 캠페인 발송 배치를 실행한다")
        void executeCampaign_AsAdmin() {
                // given (setUp에서 RUNNING 상태, 과거 시간으로 생성됨)
                mockLogin(adminMemberId);

                // when
                ResponseEntity<String> response = promotionController.executeCampaign(campaignId);

                // then
                assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
                assertThat(response.getBody()).contains("성공적으로 실행되었습니다");
        }

        @Test
        @DisplayName("USER 권한으로 캠페인 발송 배치 실행 시 AccessDeniedException이 발생한다")
        void executeCampaign_AsUser_Forbidden() {
                // given
                mockLogin(userMemberId);

                // when & then
                assertThatThrownBy(() -> promotionController.executeCampaign(campaignId))
                                .isInstanceOf(AccessDeniedException.class);
        }

        @Test
        @DisplayName("이메일 클릭 추적은 누구나 가능하다 (인증 불필요)")
        void trackEmailClick_Public() {
                // given
                Member member = entityManager.find(Member.class, userMemberId);
                Campaign campaign = entityManager.find(Campaign.class, campaignId);
                MessageSendLog log = messageSendLogRepository.save(MessageSendLog.builder()
                                .campaign(campaign)
                                .member(member)
                                .build());

                SecurityContextHolder.clearContext(); // 로그아웃 상태

                // when
                ResponseEntity<String> response = promotionController.trackEmailClick(log.getSendId());

                // then
                assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

                MessageSendLog updatedLog = messageSendLogRepository.findById(log.getSendId()).orElseThrow();
                assertThat(updatedLog.getSendClicked()).isNotNull();
        }

        @Test
        @DisplayName("ADMIN 권한으로 캠페인 목록을 페이징 조회한다")
        void getCampaigns_Integration() {
                // given
                mockLogin(adminMemberId);

                // 추가 캠페인 생성
                IntStream.range(0, 5).forEach(i -> {
                        campaignRepository.save(Campaign.builder()
                                        .campaignName("Paged " + i)
                                        .campaignContent("Content")
                                        .campaignScheduled(LocalDateTime.now().plusDays(1))
                                        .campaignStatus(CampaignStatus.SCHEDULED)
                                        .build());
                });

                PageRequest pageable = PageRequest.of(0, 3);

                // when
                ResponseEntity<Page<CampaignResponseDto>> response = promotionController.getCampaigns(pageable);

                // then
                assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getContent().size()).isEqualTo(3);
        }

        @Test
        @DisplayName("ADMIN 권한으로 캠페인 상세 정보를 조회한다")
        void getCampaign_Integration() {
                // given
                mockLogin(adminMemberId);

                Campaign campaign = campaignRepository.save(Campaign.builder()
                                .campaignName("Detail Campaign")
                                .campaignContent("Detail Content")
                                .campaignScheduled(LocalDateTime.now().minusHours(1))
                                .campaignStatus(CampaignStatus.RUNNING)
                                .build());

                Member member = entityManager.find(Member.class, userMemberId);
                messageSendLogRepository.save(MessageSendLog.builder()
                                .campaign(campaign)
                                .member(member)
                                .build());

                // when
                ResponseEntity<CampaignDetailResponseDto> response = promotionController
                                .getCampaign(campaign.getCampaignId());

                // then
                assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
                assertThat(response.getBody().getCampaignName()).isEqualTo("Detail Campaign");
                assertThat(response.getBody().getTotalSentCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("ADMIN 권한으로 캠페인 발송 이력을 조회한다")
        void getCampaignLogs_Integration() {
                // given
                mockLogin(adminMemberId);

                Campaign campaign = campaignRepository.save(Campaign.builder()
                                .campaignName("Log Campaign")
                                .campaignContent("Content")
                                .campaignScheduled(LocalDateTime.now().minusHours(1))
                                .campaignStatus(CampaignStatus.RUNNING)
                                .build());

                Member member = entityManager.find(Member.class, userMemberId);
                messageSendLogRepository.save(MessageSendLog.builder()
                                .campaign(campaign)
                                .member(member)
                                .build());

                PageRequest pageable = PageRequest.of(0, 10);

                // when
                ResponseEntity<Page<MessageSendLogResponseDto>> response = promotionController
                                .getCampaignLogs(campaign.getCampaignId(), pageable);

                // then
                assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
                assertThat(response.getBody().getTotalElements()).isEqualTo(1);
                assertThat(response.getBody().getContent().get(0).getMemberId()).isEqualTo(userMemberId);
        }
}
