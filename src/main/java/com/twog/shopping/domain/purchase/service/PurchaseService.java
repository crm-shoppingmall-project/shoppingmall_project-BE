package com.twog.shopping.domain.purchase.service;

import com.twog.shopping.domain.cart.entity.Cart;
import com.twog.shopping.domain.cart.entity.CartItem;
import com.twog.shopping.domain.cart.repository.CartItemRepository;
import com.twog.shopping.domain.cart.repository.CartRepository;
import com.twog.shopping.domain.product.entity.Product;
import com.twog.shopping.domain.product.repository.ProductRepository;
import com.twog.shopping.domain.purchase.dto.PurchaseRequest;
import com.twog.shopping.domain.purchase.dto.PurchaseResponse;
import com.twog.shopping.domain.purchase.entity.Purchase;
import com.twog.shopping.domain.purchase.entity.PurchaseDetail;
import com.twog.shopping.domain.purchase.entity.PurchaseStatus;
import com.twog.shopping.domain.purchase.repository.PurchaseRepository;
import com.twog.shopping.global.error.exception.OutOfStockException;
import com.twog.shopping.global.error.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public Purchase createPurchase(PurchaseRequest request, Long memberId) {

        Purchase purchase = Purchase.builder()
                .memberId(memberId)
                .status(PurchaseStatus.REQUESTED)
                .build();

        for (PurchaseRequest.PurchaseItemDto itemDto : request.getItems()) {

            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new NoSuchElementException("상품 정보를 찾을 수 없습니다. (ID: " + itemDto.getProductId() + ")"));

            if (!product.isStock(itemDto.getQuantity())) {
                throw new OutOfStockException("상품의 재고가 부족합니다. (ID: " + product.getProductId() + ")");
            }
            product.decreaseStock(itemDto.getQuantity());

            int itemActualPrice = product.getProductPrice();

            PurchaseDetail detail = PurchaseDetail.builder()
                    .productId(itemDto.getProductId())
                    .quantity(itemDto.getQuantity())
                    .paidAmount(itemActualPrice)
                    .build();

            purchase.addDetail(detail);
        }

        Purchase savedPurchase = purchaseRepository.save(purchase);

        Integer serverCalculatedTotal = calculateTotalAmount(savedPurchase);

        if (!Objects.equals(serverCalculatedTotal, request.getTotalAmount())) {
            throw new IllegalStateException("총 결제 금액 불일치. 위변조가 의심됩니다. (서버 계산: " + serverCalculatedTotal + ", 요청: " + request.getTotalAmount() + ")");
        }

<<<<<<< HEAD
        return savedPurchase.getId();
=======
//        return savedPurchase.getId(); // 수정: 저장된 객체의 ID 반환
        return savedPurchase;
>>>>>>> d9c78f8ca8cb9fe91690a73469a1cc9267106256
    }

    @Transactional
    public Long createPurchaseFromCart(Long memberId) {
        // memberId를 int로 캐스팅하여 findByMember_MemberId에 전달
        Cart cart = cartRepository.findByMember_MemberId(memberId.intValue())
                .orElseThrow(() -> new ResourceNotFoundException("장바구니를 찾을 수 없습니다."));

        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("장바구니에 상품이 없습니다.");
        }

        Purchase purchase = Purchase.builder()
                .memberId(memberId)
                .status(PurchaseStatus.REQUESTED)
                .build();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int quantity = cartItem.getCartItemQuantity();

            if (!product.isStock(quantity)) {
                throw new OutOfStockException("상품의 재고가 부족합니다. (ID: " + product.getProductId() + ")");
            }
            product.decreaseStock(quantity);

            PurchaseDetail detail = PurchaseDetail.builder()
                    .productId(product.getProductId())
                    .quantity(quantity)
                    .paidAmount(product.getProductPrice())
                    .build();
            purchase.addDetail(detail);
        }

        Purchase savedPurchase = purchaseRepository.save(purchase);

        // 장바구니 비우기
        cartItemRepository.deleteAll(cartItems);
        cart.getCartItems().clear();

        return savedPurchase.getId();
    }


    public Page<PurchaseResponse> findMyPurchases(Long memberId, Pageable pageable) {
        Page<Purchase> purchases = purchaseRepository.findByMemberId(memberId, pageable);
        return purchases.map(PurchaseResponse::fromEntity);
    }

    @Transactional
    public void cancelPurchase(Long purchaseId, Long memberId) {
        Purchase purchase = findAndValidateOwner(purchaseId, memberId);

        if (purchase.getStatus() != PurchaseStatus.REQUESTED) {
            throw new IllegalStateException("주문을 취소할 수 없는 상태입니다.");
        }

        purchase.updateStatus(PurchaseStatus.REJECTED);

        for (PurchaseDetail detail : purchase.getDetails()) {
            Product product = productRepository.findById(detail.getProductId())
                    .orElseThrow(() -> new NoSuchElementException("상품 정보를 찾을 수 없습니다. (ID: " + detail.getProductId() + ")"));
            product.decreaseStock(-detail.getQuantity());
        }
    }

    @Transactional
    public void requestReturn(Long purchaseId, Long memberId) {
        Purchase purchase = findAndValidateOwner(purchaseId, memberId);

        if (purchase.getStatus() != PurchaseStatus.COMPLETED) {
            throw new IllegalStateException("반품/교환을 요청할 수 없는 상태입니다.");
        }
        purchase.updateStatus(PurchaseStatus.REJECTED);
    }

    public Integer calculateTotalAmount(Purchase purchase) {
        return purchase.getDetails().stream()
                .mapToInt(detail -> detail.getPaidAmount() * detail.getQuantity())
                .sum();
    }

    public Purchase findById(Long purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NoSuchElementException("주문 정보를 찾을 수 없습니다. (ID: " + purchaseId + ")"));
    }

    private Purchase findAndValidateOwner(Long purchaseId, Long memberId) {
        Purchase purchase = findById(purchaseId);
        if (!Objects.equals(purchase.getMemberId(), memberId)) {
            throw new SecurityException("주문에 대한 권한이 없습니다.");
        }
        return purchase;
    }
}
