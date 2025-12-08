package com.twog.shopping.domain.purchase.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twog.shopping.domain.cart.repository.CartItemRepository;
import com.twog.shopping.domain.cart.repository.CartRepository;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.MemberGrade;
import com.twog.shopping.domain.member.entity.MemberStatus;
import com.twog.shopping.domain.member.entity.UserRole;
import com.twog.shopping.domain.member.repository.MemberGradeRepository;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.domain.payment.repository.PaymentRepository;
import com.twog.shopping.domain.product.entity.Product;
import com.twog.shopping.domain.product.repository.ProductRepository;
import com.twog.shopping.domain.purchase.dto.PurchaseRequest;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import com.twog.shopping.domain.purchase.repository.PurchaseDetailRepository;
import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import com.twog.shopping.domain.purchase.repository.ReturnRequestRepository;
import com.twog.shopping.domain.purchase.service.PurchaseService;
import com.twog.shopping.global.common.entity.GradeName;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class PurchaseControllerTest { // 클래스 이름 변경

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext; // WebApplicationContext 주입

    // ObjectMapper를 필드로 사용하지 않고, 각 메서드 내에서 필요할 때 생성합니다.
    // private ObjectMapper objectMapper; // 제거

    @Autowired
    private PurchaseService purchaseService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private PurchaseDetailRepository purchaseDetailRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ReturnRequestRepository returnRequestRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private MemberGradeRepository memberGradeRepository;

    private final Long TEST_MEMBER_ID = 1L;
    private final String TEST_MEMBER_EMAIL = "test@example.com";
    private final int PRODUCT_PRICE = 10000;
    private final int INITIAL_PRODUCT_QUANTITY = 100;

    private Member testMember;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // MockMvc 수동 설정
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 데이터 클리어 (외래 키 제약 조건에 따른 순서)
        paymentRepository.deleteAllInBatch();
        returnRequestRepository.deleteAllInBatch();
        purchaseDetailRepository.deleteAllInBatch();
        cartItemRepository.deleteAllInBatch();
        cartRepository.deleteAllInBatch();
        purchaseRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        memberGradeRepository.deleteAllInBatch();

        MemberGrade memberGrade = memberGradeRepository.save(MemberGrade.builder()
                .gradeName(GradeName.BRONZE)
                .gradeDesc("브론즈 등급")
                .build());

        testMember = memberRepository.saveAndFlush(Member.builder()
                .memberName("테스트유저")
                .memberEmail(TEST_MEMBER_EMAIL)
                .memberPwd("hashed_password")
                .memberGrade(memberGrade)
                .memberRole(UserRole.USER)
                .memberStatus(MemberStatus.active)
                .build());

        testProduct = productRepository.saveAndFlush(Product.builder()
                .productName("테스트 상품")
                .productCategory("테스트 카테고리")
                .productPrice(PRODUCT_PRICE)
                .productQuantity(INITIAL_PRODUCT_QUANTITY)
                .build());
    }

    @AfterEach
    void tearDown() {
        // 데이터 클리어 (외래 키 제약 조건에 따른 순서)
        paymentRepository.deleteAllInBatch();
        returnRequestRepository.deleteAllInBatch();
        purchaseDetailRepository.deleteAllInBatch();
        cartItemRepository.deleteAllInBatch();
        cartRepository.deleteAllInBatch();
        purchaseRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        memberGradeRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("POST /api/v1/purchases - 주문 생성 성공")
    @WithMockUser(username = TEST_MEMBER_EMAIL)
    void createPurchase_Success() throws Exception {
        // Given
        ObjectMapper localObjectMapper = new ObjectMapper(); // 로컬 ObjectMapper 인스턴스 생성
        int orderQuantity = 2;
        Integer expectedTotal = PRODUCT_PRICE * orderQuantity;

        PurchaseRequest.PurchaseItemDto itemDto = new PurchaseRequest.PurchaseItemDto(
                (long) testProduct.getProductId(), // int -> Long 캐스팅
                orderQuantity,
                PRODUCT_PRICE
        );
        PurchaseRequest request = new PurchaseRequest(expectedTotal, Collections.singletonList(itemDto));

        // When
        mockMvc.perform(post("/api/v1/purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(localObjectMapper.writeValueAsString(request))) // 로컬 ObjectMapper 사용
                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("주문이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data").isNumber()); // 반환되는 purchaseId가 숫자 타입인지 확인

        // DB에서 실제 데이터 확인
        List<Purchase> purchases = purchaseRepository.findAll();
        assertThat(purchases).hasSize(1);
        Purchase savedPurchase = purchases.get(0);
        assertThat(savedPurchase.getMemberId()).isEqualTo(testMember.getMemberId());
        assertThat(savedPurchase.getStatus()).isEqualTo(PurchaseStatus.REQUESTED);
        assertThat(savedPurchase.getDetails()).hasSize(1);
        assertThat(savedPurchase.getDetails().get(0).getProductId()).isEqualTo(testProduct.getProductId());

        Product updatedProduct = productRepository.findById(testProduct.getProductId()).orElseThrow();
        assertThat(updatedProduct.getProductQuantity()).isEqualTo(INITIAL_PRODUCT_QUANTITY - orderQuantity);
    }

    @Test
    @DisplayName("GET /api/v1/purchases - 내 주문 목록 조회 성공")
    @WithMockUser(username = TEST_MEMBER_EMAIL)
    void getMyPurchases_Success() throws Exception {
        // Given
        // 테스트를 위해 주문을 미리 생성합니다.
        purchaseService.createPurchase(new PurchaseRequest(PRODUCT_PRICE, List.of(new PurchaseRequest.PurchaseItemDto((long) testProduct.getProductId(), 1, PRODUCT_PRICE))), testMember.getMemberId());
        purchaseService.createPurchase(new PurchaseRequest(PRODUCT_PRICE * 2, List.of(new PurchaseRequest.PurchaseItemDto((long) testProduct.getProductId(), 2, PRODUCT_PRICE))), testMember.getMemberId());

        // When
        mockMvc.perform(get("/api/v1/purchases")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("주문 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].memberId").value(testMember.getMemberId()))
                .andExpect(jsonPath("$.data.content[0].totalAmount").value(PRODUCT_PRICE))
                .andExpect(jsonPath("$.data.content[1].totalAmount").value(PRODUCT_PRICE * 2));
    }

    @Test
    @DisplayName("POST /api/v1/purchases/{purchaseId}/cancel - 주문 취소 성공")
    @WithMockUser(username = TEST_MEMBER_EMAIL)
    void cancelPurchase_Success() throws Exception {
        // Given
        // 테스트를 위해 주문을 미리 생성합니다.
        Long purchaseId = purchaseService.createPurchase(new PurchaseRequest(PRODUCT_PRICE, List.of(new PurchaseRequest.PurchaseItemDto((long) testProduct.getProductId(), 1, PRODUCT_PRICE))), testMember.getMemberId());

        // When
        mockMvc.perform(post("/api/v1/purchases/{purchaseId}/cancel", purchaseId)
                        .contentType(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("주문이 성공적으로 취소되었습니다."));

        // DB에서 실제 데이터 확인
        Purchase cancelledPurchase = purchaseRepository.findById(purchaseId).orElseThrow();
        assertThat(cancelledPurchase.getStatus()).isEqualTo(PurchaseStatus.REJECTED); // PurchaseService에서 REJECTED로 변경됨

        Product updatedProduct = productRepository.findById(testProduct.getProductId()).orElseThrow();
        assertThat(updatedProduct.getProductQuantity()).isEqualTo(INITIAL_PRODUCT_QUANTITY); // 재고 복구 확인
    }

    @Test
    @DisplayName("POST /api/v1/purchases/{purchaseId}/cancel - 주문 취소 실패 (권한 없음)")
    @WithMockUser(username = "other@example.com") // 다른 사용자로 요청
    void cancelPurchase_Fail_Unauthorized() throws Exception {
        // Given
        // 테스트를 위해 주문을 미리 생성합니다.
        Long purchaseId = purchaseService.createPurchase(new PurchaseRequest(PRODUCT_PRICE, List.of(new PurchaseRequest.PurchaseItemDto((long) testProduct.getProductId(), 1, PRODUCT_PRICE))), testMember.getMemberId());

        // When & Then
        mockMvc.perform(post("/api/v1/purchases/{purchaseId}/cancel", purchaseId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // 403 Forbidden (SecurityException)
    }
}