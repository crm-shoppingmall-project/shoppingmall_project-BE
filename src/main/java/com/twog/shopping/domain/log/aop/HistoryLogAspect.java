package com.twog.shopping.domain.log.aop;

import com.twog.shopping.domain.log.entity.HistoryActionType;
import com.twog.shopping.domain.log.entity.HistoryRefTable;
import com.twog.shopping.domain.log.service.HistoryService;
import com.twog.shopping.domain.member.service.DetailsUser;
import com.twog.shopping.domain.purchase.dto.PurchaseResponse;
import com.twog.shopping.global.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class HistoryLogAspect {

    private final HistoryService historyService;

    // 해당 메소드 위에
    // @LogHistory(actionType = HistoryActionType.PURCHASE_COMPLETED) 한줄 추가하면 완성

    @AfterReturning(pointcut = "@annotation(logHistory)", returning = "result")
    public void logHistory(JoinPoint joinPoint, LogHistory logHistory, Object result) {

        HistoryActionType actionType = logHistory.actionType();

        // 1) HttpServletRequest 가져오기 (IP, User-Agent용)
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return;
        }

        HttpServletRequest request = attrs.getRequest();

        Long memberId = getCurrentMemberId();
        if (memberId == null) {
            log.debug("[HistoryLogAspect] memberId가 없습니다, skip logging");
            return;
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("method", joinPoint.getSignature().toShortString());
        detail.put("path", request.getRequestURI());

        Long refId = null;

        if (actionType == HistoryActionType.PURCHASE_COMPLETED) {

            refId = extractPurchaseInfo(joinPoint,result, detail);

        }

        historyService.saveHistory(
                memberId,
                actionType,
                detail,
                request,
                HistoryRefTable.purchase,
                refId

        );


    }


    private Long getCurrentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) return null;

        Object principal = auth.getPrincipal();

        if (principal instanceof DetailsUser detailsUser) {

            return detailsUser.getMember().getMemberId();
        }

        return null;

    }

    private Long extractPurchaseInfo(JoinPoint joinPoint, Object result, Map<String, Object> detail) {

        try {

            Object body = result;

            if (body != null) {

                //  ResponseEntity<ApiResponse<PurchaseResponse>> 인 경우
                if (body instanceof ResponseEntity<?> responseEntity) {
                    body = responseEntity.getBody();   // ApiResponse<PurchaseResponse>
                    if (body == null) {
                        log.debug("[HistoryLogAspect] ResponseEntity body가 null 입니다.");
                    }
                }

                //  ApiResponse<PurchaseResponse> 인 경우
                if (body instanceof ApiResponse<?> apiResponse) {
                    Object data = apiResponse.getData();  // PurchaseResponse

                    if (data instanceof PurchaseResponse purchase) {
                        return fillPurchaseDetail(purchase, detail);
                    } else {
                        log.debug("[HistoryLogAspect] ApiResponse.data가 PurchaseResponse가 아닙니다. type={}",
                                (data != null ? data.getClass().getName() : "null"));
                    }
                }

                //  그냥 PurchaseResponse 인 경우
                if (body instanceof PurchaseResponse purchase) {
                    return fillPurchaseDetail(purchase, detail);
                }

                // 여기까지 왔는데 body가 null이 아니고, 위 타입에도 안 걸리면
                if (body != null) {
                    log.debug("[HistoryLogAspect] 지원하지 않는 반환 타입입니다: {}", body.getClass().getName());
                }
            }

            // ============================
            // 2) result로 못 찾았으면 → 파라미터(args)에서 fallback
            //    (PaymentService.confirmTossPayment 같은 void 메서드 지원)
            // ============================
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {

                // case 1: 이미 PurchaseResponse가 인자로 넘어오는 경우
                if (arg instanceof PurchaseResponse purchase) {
                    return fillPurchaseDetail(purchase, detail);
                }

                // case 2: purchaseId / paymentId 후보 (Long)
                if (arg instanceof Long id) {
                    detail.put("purchaseIdGuess", id);
                    return id;
                }

                // case 3: orderId 처럼 String 숫자형 파라미터
                if (arg instanceof String s && s.matches("\\d+")) {
                    Long id = Long.valueOf(s);
                    detail.put("orderId", s);
                    detail.put("purchaseIdGuess", id);  // paymentId일 수도 있으니 Guess
                    return id;
                }
            }

            log.debug("[HistoryLogAspect] 결과/파라미터에서 구매 정보를 추출하지 못했습니다.");
            return null;

        } catch (Exception e) {
            log.debug("[HistoryLogAspect] 결과에서 구매 정보를 추출하는 데 실패했습니다.: {}", e.getMessage());
            return null;
        }
    }


    private Long fillPurchaseDetail(PurchaseResponse purchase, Map<String, Object> detail) {
        detail.put("purchaseId", purchase.getPurchaseId());
        detail.put("memberId", purchase.getMemberId());
        detail.put("status", purchase.getStatus());
        detail.put("totalAmount", purchase.getTotalAmount());
        if (purchase.getCreatedAt() != null) {
            detail.put("createdAt", purchase.getCreatedAt().toString());
            // 필요하면 포맷 지정도 가능:
            // detail.put("createdAt", purchase.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        detail.put("items", purchase.getDetails());

        return purchase.getPurchaseId();
    }




}

