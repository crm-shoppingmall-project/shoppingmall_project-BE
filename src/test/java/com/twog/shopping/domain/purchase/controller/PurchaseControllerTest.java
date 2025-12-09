package com.twog.shopping.domain.purchase.controller;

import com.twog.shopping.domain.analytics.repository.MemberGradeHistoryRepository;
import com.twog.shopping.domain.analytics.repository.MemberRfmRepository;
import com.twog.shopping.domain.cart.entity.Cart;
import com.twog.shopping.domain.cart.entity.CartItem;
import com.twog.shopping.domain.cart.repository.CartItemRepository;
import com.twog.shopping.domain.cart.repository.CartRepository;
import com.twog.shopping.domain.log.repository.HistoryRepository; // HistoryRepository import 추가
import com.twog.shopping.domain.member.entity.Member;
import com.twog.shopping.domain.member.entity.MemberGrade;
import com.twog.shopping.domain.member.entity.MemberStatus;
import com.twog.shopping.domain.member.entity.UserRole;
import com.twog.shopping.domain.member.repository.MemberGradeRepository;
import com.twog.shopping.domain.member.repository.MemberProfileRepository; // MemberProfileRepository import 추가
import com.twog.shopping.domain.member.repository.MemberRepository;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.domain.payment.repository.PaymentRepository;
import com.twog.shopping.domain.product.entity.Product;
import com.twog.shopping.domain.product.entity.ProductStatus;
import com.twog.shopping.domain.product.repository.ProductRepository;
import com.twog.shopping.domain.promotion.repository.MessageSendLogRepository; // MessageSendLogRepository import 추가
import com.twog.shopping.domain.purchase.dto.PurchaseResponse;
import com.twog.shopping.domain.purchase.entity.Purchase;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User; // Spring Security User import
import org.springframework.security.test.context.support.WithMockUser; // WithMockUser import 추가
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PurchaseControllerTest {

    @Autowired
    private PurchaseController purchaseController;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PurchaseDetailRepository purchaseDetailRepository;
    @Autowired
    private MemberGradeRepository memberGradeRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ReturnRequestRepository returnRequestRepository;
    @Autowired
    private CsTicketRepository csTicketRepository;
    @Autowired
    private CsTicketReplyRepository csTicketReplyRepository;
    @Autowired
    private MemberGradeHistoryRepository memberGradeHistoryRepository;
    @Autowired
    private MemberRfmRepository memberRfmRepository;
    @Autowired
    private MessageSendLogRepository messageSendLogRepository; // MessageSendLogRepository 주입
    @Autowired
    private MemberProfileRepository memberProfileRepository; // MemberProfileRepository 주입
    @Autowired
    private HistoryRepository historyRepository; // HistoryRepository 주입
    @Autowired
    private EntityManager entityManager;

    // --- 테스트용 상수 및 변수 ---
    private Long userMemberId;
    private Product testProduct; // setUp에서 생성된 Product를 저장
    private Member testMember; // setUp에서 생성된 Member를 저장

    private final String TEST_MEMBER_EMAIL = "user@test.com"; // setUp에서 생성될 user의 이메일
    private final int INITIAL_PRODUCT_PRICE = 10000;
    private final int INITIAL_PRODUCT_QUANTITY = 100;

    @BeforeEach
    void setUp() {
        // 데이터 클리어 (외래 키 제약 조건에 따른 올바른 순서)
        historyRepository.deleteAllInBatch(); // History 데이터 먼저 삭제
        messageSendLogRepository.deleteAllInBatch(); // MessageSendLog 데이터 먼저 삭제
        memberProfileRepository.deleteAllInBatch(); // MemberProfile 데이터 먼저 삭제
        entityManager.createNativeQuery("DELETE FROM Segment_member").executeUpdate(); // Segment_member 데이터 삭제

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
        testMember = memberRepository.save(Member.builder()
                .memberName("테스트유저")
                .memberEmail(TEST_MEMBER_EMAIL)
                .memberPwd("password") // 비밀번호는 BCryptPasswordEncoder로 인코딩되어야 하지만, 테스트에서는 단순 문자열 사용
                .memberGrade(memberGrade) // 올바르게 생성된 memberGrade 사용
                .memberRole(UserRole.USER)
                .memberStatus(MemberStatus.active)
                .memberBirth(LocalDate.of(1990, 1, 1))
                .memberCreated(LocalDateTime.now())
                .memberUpdated(LocalDateTime.now())
                .memberLastAt(LocalDateTime.now())
                .memberPhone("01012345678") // memberPhone 추가
                .build());
        userMemberId = testMember.getMemberId(); // testMember 할당 후 userMemberId 할당


        // 3. 테스트 상품 생성 (Product1)
        testProduct = productRepository.save(Product.builder()
                .productName("Test Product 1")
                .productCategory("Category A")
                .productPrice(INITIAL_PRODUCT_PRICE)
                .productQuantity(INITIAL_PRODUCT_QUANTITY)
                .productStatus(ProductStatus.ACTIVE)
                .build());

        // 4. 테스트용 장바구니 생성 및 상품 추가
        Cart testCart = Cart.createCart(testMember);
        cartRepository.save(testCart);

        CartItem cartItem = CartItem.createCartItem(testCart, testProduct, 2);
        cartItemRepository.save(cartItem);

        entityManager.flush();
        entityManager.clear();
    }

    // Helper: SecurityContext에 인증 정보 설정
    private User mockLogin(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(); // DB에서 Member를 다시 로드
        DetailsUser detailsUser = new DetailsUser(member);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                detailsUser, null, detailsUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return new User(detailsUser.getUsername(), detailsUser.getPassword(), detailsUser.getAuthorities());
    }

//    @Test
//    @DisplayName("POST /api/v1/purchases/from-cart - 장바구니에서 상품 주문 생성 성공")
//    @WithMockUser(username = TEST_MEMBER_EMAIL) // @WithMockUser 추가
//    void createPurchaseFromCart_Success() throws Exception {
//        User user = mockLogin(userMemberId); // 테스트 전에 로그인 Mocking
//
//        // When
//        ResponseEntity<ApiResponse<Long>> response = purchaseController.createPurchaseFromCart(user);
//
//        // Then
//        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
//        assertThat(response.getBody()).isNotNull();
//        assertThat(response.getBody().getStatus()).isEqualTo("Created"); // HttpStatus.CREATED.getReasonPhrase()는 "Created"
//        assertThat(response.getBody().getMessage()).isEqualTo("장바구니 상품으로 주문이 성공적으로 생성되었습니다.");
//        assertThat(response.getBody().getData()).isNotNull(); // purchaseId가 반환됨
//
//        // DB 검증
//        List<Purchase> purchases = purchaseRepository.findAll();
//        assertThat(purchases).hasSize(1);
//        assertThat(purchases.get(0).getMemberId()).isEqualTo(userMemberId);
//        assertThat(purchases.get(0).getStatus()).isEqualTo(PurchaseStatus.REQUESTED);
//        assertThat(purchases.get(0).getDetails()).hasSize(1);
//        assertThat(purchases.get(0).getDetails().get(0).getProductId()).isEqualTo(testProduct.getProductId());
//
//        // 장바구니가 비워졌는지 확인
//        Cart updatedCart = cartRepository.findByMember_MemberId(userMemberId.intValue()).orElseThrow(); // Long -> int 캐스팅
//        assertThat(updatedCart.getCartItems()).isEmpty();
//    }

//    @Test
//    @DisplayName("GET /api/v1/purchases - 내 주문 목록 조회 성공")
//    @WithMockUser(username = TEST_MEMBER_EMAIL) // @WithMockUser 추가
//    void getMyPurchases_Success() throws Exception {
//        User user = mockLogin(userMemberId);
//
//        // Given
//        purchaseController.createPurchaseFromCart(user);
//        entityManager.flush();
//        entityManager.clear();
//
//        // When
//        ResponseEntity<ApiResponse<Page<PurchaseResponse>>> response =
//                purchaseController.getMyPurchases(user, PageRequest.of(0, 10)); // PageRequest import 및 사용
//
//        // Then
//        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
//        assertThat(response.getBody()).isNotNull();
//        assertThat(response.getBody().getStatus()).isEqualTo("OK"); // HttpStatus.OK.getReasonPhrase()는 "OK"
//        assertThat(response.getBody().getMessage()).isEqualTo("주문 목록 조회가 완료되었습니다.");
//        assertThat(response.getBody().getData().getContent()).hasSize(1);
//        assertThat(response.getBody().getData().getContent().get(0).getMemberId()).isEqualTo(userMemberId);
//    }

//    @Test
//    @DisplayName("POST /api/v1/purchases/{purchaseId}/cancel - 주문 취소 성공")
//    @WithMockUser(username = TEST_MEMBER_EMAIL)
//    void cancelPurchase_Success() throws Exception {
//        User user = mockLogin(userMemberId); // 테스트 전에 로그인 Mocking
//
//        // Given: 테스트용 주문 데이터 생성 (장바구니에서 구매 생성)
//        ResponseEntity<ApiResponse<Long>> createResponse = purchaseController.createPurchaseFromCart(user);
//        Long purchaseId = createResponse.getBody().getData();
//        entityManager.flush();
//        entityManager.clear();
//
//        // When
//        ResponseEntity<ApiResponse<Void>> response = purchaseController.cancelPurchase(purchaseId, user);
//
//        // Then
//        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
//        assertThat(response.getBody()).isNotNull();
//        assertThat(response.getBody().getStatus()).isEqualTo("OK"); // HttpStatus.OK.getReasonPhrase()는 "OK"
//        assertThat(response.getBody().getMessage()).isEqualTo("주문이 성공적으로 취소되었습니다.");
//
//        entityManager.flush();
//        entityManager.clear();
//
//        // DB 검증
//        Purchase cancelledPurchase = purchaseRepository.findById(purchaseId).orElseThrow();
//        assertThat(cancelledPurchase.getStatus()).isEqualTo(PurchaseStatus.REJECTED);
//
//        // 재고 복원 검증
//        Product productAfterCancel = productRepository.findById(testProduct.getProductId()).orElseThrow();
//        assertThat(productAfterCancel.getProductQuantity()).isEqualTo(INITIAL_PRODUCT_QUANTITY);
//    }
}
