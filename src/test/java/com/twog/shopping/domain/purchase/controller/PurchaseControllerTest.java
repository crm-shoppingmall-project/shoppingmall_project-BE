package com.twog.shopping.domain.purchase.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.MemberGrade;
import com.twog.shopping.domain.member.entity.MemberStatus;
import com.twog.shopping.domain.member.entity.UserRole;
import com.twog.shopping.domain.member.repository.MemberGradeRepository;
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.domain.product.entity.Product;
import com.twog.shopping.domain.product.entity.ProductStatus;
import com.twog.shopping.domain.product.repository.ProductRepository;
import com.twog.shopping.domain.purchase.dto.PurchaseRequest;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import com.twog.shopping.domain.purchase.repository.PurchaseDetailRepository;
import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import com.twog.shopping.global.common.entity.GradeName;
import jakarta.persistence.EntityManager;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class PurchaseControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    // --- 실제 Bean 주입 ---
    @Autowired private MemberRepository memberRepository;
    @Autowired private PurchaseRepository purchaseRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private PurchaseDetailRepository purchaseDetailRepository;
    @Autowired private MemberGradeRepository memberGradeRepository;
    @Autowired private EntityManager entityManager;

    // --- 테스트용 상수 및 변수 ---
    private Member testMember;
    private Product testProduct;
    private PurchaseRequest purchaseRequest;

    private final String TEST_MEMBER_EMAIL = "test@example.com";
    private final String TEST_MEMBER_PWD = "password123";
    private final int INITIAL_PRODUCT_PRICE = 10000;
    private final int INITIAL_PRODUCT_QUANTITY = 100;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // 데이터 클리어
        purchaseDetailRepository.deleteAllInBatch();
        purchaseRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        memberGradeRepository.deleteAllInBatch();

        // 테스트 데이터 생성
        MemberGrade memberGrade = memberGradeRepository.save(MemberGrade.builder()
                .gradeName(GradeName.BRONZE)
                .gradeDesc("브론즈 등급")
                .build());

        testMember = memberRepository.save(Member.builder()
                .memberName("테스트유저")
                .memberEmail(TEST_MEMBER_EMAIL)
                .memberPwd(TEST_MEMBER_PWD)
                .memberGrade(memberGrade)
                .memberRole(UserRole.USER)
                .memberStatus(MemberStatus.active)
                .memberBirth(LocalDate.of(1990, 1, 1))
                .memberCreated(LocalDateTime.now())
                .build());

        testProduct = productRepository.save(Product.builder()
                .productName("테스트 상품")
                .productCategory("테스트 카테고리")
                .productPrice(INITIAL_PRODUCT_PRICE)
                .productQuantity(INITIAL_PRODUCT_QUANTITY)
                .productStatus(ProductStatus.ACTIVE)
                .build());

        PurchaseRequest.PurchaseItemDto itemDto = new PurchaseRequest.PurchaseItemDto(
                testProduct.getProductId(),
                2,
                INITIAL_PRODUCT_PRICE
        );

        purchaseRequest = new PurchaseRequest(
                INITIAL_PRODUCT_PRICE * 2,
                Collections.singletonList(itemDto)
        );
    }

    @Test
    @DisplayName("POST /api/v1/purchases - 상품 주문 생성 성공")
    @WithMockUser(username = TEST_MEMBER_EMAIL)
    void createPurchase_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/purchases")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.message").value("주문이 성공적으로 생성되었습니다."))
                .andExpect(jsonPath("$.data").isNumber());

        // DB 검증
        List<Purchase> purchases = purchaseRepository.findAll();
        assertThat(purchases).hasSize(1);
        assertThat(purchases.get(0).getMemberId()).isEqualTo(testMember.getMemberId());
        assertThat(purchases.get(0).getStatus()).isEqualTo(PurchaseStatus.REQUESTED);
        assertThat(purchases.get(0).getDetails()).hasSize(1);
        assertThat(purchases.get(0).getDetails().get(0).getProductId()).isEqualTo(testProduct.getProductId());
    }

    @Test
    @DisplayName("GET /api/v1/purchases - 내 주문 목록 조회 성공")
    @WithMockUser(username = TEST_MEMBER_EMAIL)
    void getMyPurchases_Success() throws Exception {
        // Given: 테스트용 주문 데이터 생성
        mockMvc.perform(post("/api/v1/purchases")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(purchaseRequest)));

        // When & Then
        mockMvc.perform(get("/api/v1/purchases")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("주문 목록 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].memberId").value(testMember.getMemberId()));
    }

    @Test
    @DisplayName("POST /api/v1/purchases/{purchaseId}/cancel - 주문 취소 성공")
    @WithMockUser(username = TEST_MEMBER_EMAIL)
    void cancelPurchase_Success() throws Exception {
        // Given: 테스트용 주문 데이터 생성
        String response = mockMvc.perform(post("/api/v1/purchases")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchaseRequest)))
                .andReturn().getResponse().getContentAsString();

        // 응답에서 purchaseId 추출
        String purchaseIdStr = response.split(":")[3].replace("}", "").trim();
        Long purchaseId = Long.parseLong(purchaseIdStr);

        // When & Then
        mockMvc.perform(post("/api/v1/purchases/{purchaseId}/cancel", purchaseId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("주문이 성공적으로 취소되었습니다."));

        entityManager.flush();
        entityManager.clear();

        // DB 검증
        Purchase cancelledPurchase = purchaseRepository.findById(purchaseId).orElseThrow();
        assertThat(cancelledPurchase.getStatus()).isEqualTo(PurchaseStatus.REJECTED);

        // 재고 복원 검증
        Product productAfterCancel = productRepository.findById(testProduct.getProductId()).orElseThrow();
        assertThat(productAfterCancel.getProductQuantity()).isEqualTo(INITIAL_PRODUCT_QUANTITY);
    }
}
