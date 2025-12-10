package com.twog.shopping.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twog.shopping.domain.analytics.repository.MemberGradeHistoryRepository;
import com.twog.shopping.domain.analytics.repository.MemberRfmRepository;
import com.twog.shopping.domain.cart.repository.CartItemRepository;
import com.twog.shopping.domain.cart.repository.CartRepository;
import com.twog.shopping.domain.log.repository.HistoryRepository;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.MemberGrade;
import com.twog.shopping.domain.member.entity.MemberStatus;
import com.twog.shopping.domain.member.entity.UserRole;
import com.twog.shopping.domain.member.repository.MemberGradeRepository;
import com.twog.shopping.domain.member.repository.MemberProfileRepository;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.domain.payment.dto.PaymentRequest;
import com.twog.shopping.domain.payment.entity.Payment;
import com.twog.shopping.domain.payment.entity.PaymentStatus;
import com.twog.shopping.domain.payment.entity.PaymentType;
import com.twog.shopping.domain.payment.repository.PaymentRepository;
import com.twog.shopping.domain.payment.service.PaymentService;
import com.twog.shopping.domain.product.entity.Product;
import com.twog.shopping.domain.product.entity.ProductStatus;
import com.twog.shopping.domain.product.repository.ProductRepository;
import com.twog.shopping.domain.promotion.repository.MessageSendLogRepository;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseDetail;
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import com.twog.shopping.domain.purchase.repository.PurchaseDetailRepository;
import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import com.twog.shopping.domain.purchase.repository.ReturnRequestRepository;
import com.twog.shopping.domain.support.repository.CsTicketReplyRepository;
import com.twog.shopping.domain.support.repository.CsTicketRepository;
import com.twog.shopping.global.common.dto.ApiResponse;
import com.twog.shopping.global.common.entity.GradeName;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class PaymentControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    // --- 실제 Bean 주입 ---
    @Autowired private PaymentService paymentService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PurchaseRepository purchaseRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private PurchaseDetailRepository purchaseDetailRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private MemberGradeRepository memberGradeRepository;
    @Autowired private ReturnRequestRepository returnRequestRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CsTicketRepository csTicketRepository;
    @Autowired private CsTicketReplyRepository csTicketReplyRepository;
    @Autowired private MemberGradeHistoryRepository memberGradeHistoryRepository;
    @Autowired private MemberRfmRepository memberRfmRepository;
    @Autowired private MessageSendLogRepository messageSendLogRepository;
    @Autowired private MemberProfileRepository memberProfileRepository;
    @Autowired private HistoryRepository historyRepository;
    @Autowired private EntityManager entityManager;

    // --- 테스트용 상수 및 변수 ---
    private Member testMember; // testMember 필드 추가
    private Long userMemberId;
    private Product testProduct;
    private Purchase testPurchase;
    private PaymentRequest paymentRequest;

    private final String TEST_MEMBER_EMAIL = "test@example.com";
    private final String TEST_MEMBER_PWD = "password123";
    private final int INITIAL_PRODUCT_PRICE = 10000;
    private final int INITIAL_PRODUCT_QUANTITY = 100;
    private final String TEST_PAYMENT_KEY = "test_payment_key_from_toss";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 데이터 클리어 (외래 키 제약 조건에 따른 올바른 순서)
        historyRepository.deleteAllInBatch();
        messageSendLogRepository.deleteAllInBatch();
        memberProfileRepository.deleteAllInBatch();
        entityManager.createNativeQuery("DELETE FROM Segment_member").executeUpdate();

        csTicketReplyRepository.deleteAllInBatch();
        csTicketRepository.deleteAllInBatch();
        returnRequestRepository.deleteAllInBatch();
        paymentRepository.deleteAllInBatch();
        purchaseDetailRepository.deleteAllInBatch();
        purchaseRepository.deleteAllInBatch();
        cartItemRepository.deleteAllInBatch();
        cartRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        memberGradeHistoryRepository.deleteAllInBatch();
        memberRfmRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        memberGradeRepository.deleteAllInBatch();

        // 1. 등급 데이터 생성
        MemberGrade memberGrade = memberGradeRepository.save(MemberGrade.builder()
                .gradeName(GradeName.BRONZE)
                .gradeDesc("브론즈 등급")
                .build());

        // 2. USER 회원 생성
        Member member = Member.builder()
                .memberName("테스트유저")
                .memberEmail(TEST_MEMBER_EMAIL)
                .memberPwd(TEST_MEMBER_PWD)
                .memberGrade(memberGrade)
                .memberRole(UserRole.USER)
                .memberStatus(MemberStatus.active)
                .memberBirth(LocalDate.of(1990, 1, 1))
                .memberCreated(LocalDateTime.now())
                .memberUpdated(LocalDateTime.now())
                .memberLastAt(LocalDateTime.now())
                .memberPhone("01012345678")
                .memberGender('M')
                .build();
        testMember = memberRepository.save(member);
        userMemberId = testMember.getMemberId();


        // 3. 테스트 상품 생성 (Product1)
        testProduct = productRepository.save(Product.builder()
                .productName("테스트 상품")
                .productCategory("테스트 카테고리")
                .productPrice(INITIAL_PRODUCT_PRICE)
                .productQuantity(INITIAL_PRODUCT_QUANTITY)
                .productStatus(ProductStatus.ACTIVE)
                .build());

        // 4. 테스트 구매 생성
        testPurchase = Purchase.builder()
                .memberId(testMember.getMemberId())
                .status(PurchaseStatus.REQUESTED)
                .build();

        PurchaseDetail detail1 = PurchaseDetail.builder()
                .productId(testProduct.getProductId())
                .quantity(2)
                .paidAmount(INITIAL_PRODUCT_PRICE)
                .build();

        testPurchase.addDetail(detail1);
        purchaseRepository.save(testPurchase);

        // PaymentRequest는 paymentKey 필드가 없음
        paymentRequest = new PaymentRequest(
                testPurchase.getId(),
                INITIAL_PRODUCT_PRICE * 2,
                PaymentType.PAYMENT
        );

        entityManager.flush();
        entityManager.clear();
    }

    // Helper: SecurityContext에 인증 정보 설정 (ProductControllerTest와 동일한 형식)
    private void mockLogin(Long memberId) {
        Member member = entityManager.find(Member.class, memberId);
        DetailsUser detailsUser = new DetailsUser(member);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                detailsUser, null, detailsUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("POST /api/v1/payments/initiate - 결제 초기화 성공")
    void initiatePayment_Success() throws Exception {
        mockLogin(userMemberId); // USER 로그인

        // When & Then
        mockMvc.perform(post("/api/v1/payments/initiate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("결제 요청이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data.paymentId").isNumber());

        // DB 검증
        List<Payment> payments = paymentRepository.findAll();
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getStatus()).isEqualTo(PaymentStatus.REQUESTED);
        assertThat(payments.get(0).getPurchase().getId()).isEqualTo(testPurchase.getId());
        assertThat(payments.get(0).getPgTid()).isNotNull(); // pgTid가 null이 아님을 확인 (임시값)
        assertThat(payments.get(0).getPaidAt()).isNotNull(); // paidAt이 null이 아님을 확인
    }

//    @Test
//    @DisplayName("GET /api/v1/payments/confirm - 토스페이먼츠 결제 승인 성공")
//    void confirmTossPayment_Success() throws Exception {
//        mockLogin(userMemberId); // USER 로그인
//
//        // Given: 결제 초기화
//        // 실제 paymentService를 호출하여 Payment를 생성하고 ID를 얻음
//        Long paymentId = paymentService.initiatePayment(paymentRequest, userMemberId);
//        entityManager.flush();
//        entityManager.clear(); // 영속성 컨텍스트 초기화
//
//        // DB에서 Payment와 Purchase를 다시 로드
//        Payment initiatedPayment = paymentRepository.findById(paymentId).orElseThrow();
//        Purchase initiatedPurchase = purchaseRepository.findById(testPurchase.getId()).orElseThrow();
//
//        // 토스페이먼츠 API 호출 Mocking
//        when(paymentService.getTossPaymentsConfig().getApiUrl()).thenReturn("http://localhost:8080/toss"); // Mock URL
//        when(paymentService.getTossPaymentsConfig().getSecretKey()).thenReturn("test_secret_key");
//
//        ResponseEntity<Map> mockTossResponse = new ResponseEntity<>(Map.of("status", "DONE"), HttpStatus.OK);
//        when(paymentService.getRestTemplate().postForEntity(
//                anyString(),
//                any(HttpEntity.class),
//                eq(Map.class)
//        )).thenReturn(mockTossResponse);
//
//
//        // When & Then
//        mockMvc.perform(get("/api/v1/payments/confirm")
//                        .param("paymentKey", TEST_PAYMENT_KEY)
//                        .param("orderId", String.valueOf(paymentId)) // paymentId 사용
//                        .param("amount", String.valueOf(paymentRequest.getAmount())))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("OK"))
//                .andExpect(jsonPath("$.message").value("결제가 성공적으로 승인되었습니다."));
//
//        entityManager.flush();
//        entityManager.clear();
//
//        // DB 검증
//        Payment confirmedPayment = paymentRepository.findById(paymentId).orElseThrow();
//        assertThat(confirmedPayment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
//        assertThat(confirmedPayment.getPgTid()).isEqualTo(TEST_PAYMENT_KEY);
//        assertThat(confirmedPayment.getPaidAt()).isNotNull();
//
//        Purchase completedPurchase = purchaseRepository.findById(testPurchase.getId()).orElseThrow();
//        assertThat(completedPurchase.getStatus()).isEqualTo(PurchaseStatus.COMPLETED);
//    }

//    @Test
//    @DisplayName("POST /api/v1/payments/{paymentId}/cancel - 결제 취소 성공")
//    void cancelPayment_Success() throws Exception {
//        mockLogin(userMemberId); // USER 로그인
//
//        // Given: 결제 완료 상태의 데이터 생성
//        Long paymentId = paymentService.initiatePayment(paymentRequest, userMemberId);
//        paymentService.confirmTossPayment(TEST_PAYMENT_KEY, String.valueOf(paymentId), paymentRequest.getAmount());
//        entityManager.flush();
//        entityManager.clear();
//
//        // DB에서 Payment와 Purchase를 다시 로드
//        Payment completedPayment = paymentRepository.findById(paymentId).orElseThrow();
//        Purchase completedPurchase = purchaseRepository.findById(testPurchase.getId()).orElseThrow();
//
//        // 토스페이먼츠 API 호출 Mocking
//        when(paymentService.getTossPaymentsConfig().getApiUrl()).thenReturn("http://localhost:8080/toss"); // Mock URL
//        when(paymentService.getTossPaymentsConfig().getSecretKey()).thenReturn("test_secret_key");
//
//        ResponseEntity<Map> mockTossCancelResponse = new ResponseEntity<>(Map.of("status", "CANCELED"), HttpStatus.OK);
//        when(paymentService.getRestTemplate().postForEntity(
//                anyString(),
//                any(HttpEntity.class),
//                eq(Map.class)
//        )).thenReturn(mockTossCancelResponse);
//
//
//        String cancelReason = "단순 변심";
//        Map<String, String> requestBody = Map.of("cancelReason", cancelReason);
//
//        // When & Then
//        mockMvc.perform(post("/api/v1/payments/{paymentId}/cancel", paymentId)
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestBody)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("OK"))
//                .andExpect(jsonPath("$.message").value("결제가 성공적으로 취소되었습니다."));
//
//        entityManager.flush();
//        entityManager.clear();
//
//        // DB 검증
//        Payment cancelledPayment = paymentRepository.findById(paymentId).orElseThrow();
//        assertThat(cancelledPayment.getStatus()).isEqualTo(PaymentStatus.REJECTED);
//    }

    @Test
    @DisplayName("GET /api/v1/payments/{paymentId} - 특정 결제 정보 조회 성공")
    void getPaymentById_Success() throws Exception {
        mockLogin(userMemberId); // USER 로그인

        // Given: 결제 완료 상태의 데이터 생성
        Long paymentId = paymentService.initiatePayment(paymentRequest, userMemberId);
        paymentService.confirmTossPayment(TEST_PAYMENT_KEY, String.valueOf(paymentId), paymentRequest.getAmount());
        entityManager.flush();
        entityManager.clear();

        // When & Then
        mockMvc.perform(get("/api/v1/payments/{paymentId}", paymentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.paymentId").value(paymentId))
                .andExpect(jsonPath("$.data.pgTid").value(TEST_PAYMENT_KEY));
    }
}
