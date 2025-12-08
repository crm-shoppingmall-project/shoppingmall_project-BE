package com.twog.shopping.domain.log.aop;

import com.twog.shopping.domain.log.entity.HistoryActionType;
import com.twog.shopping.domain.log.entity.HistoryRefTable;
import com.twog.shopping.domain.log.service.HistoryService;
import com.twog.shopping.domain.member.service.DetailsUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
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

           refId =  extractPurchaseInfo(result,detail);

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

    private Long extractPurchaseInfo(Object result, Map<String, Object> detail) {
        if (result == null) return null;

        try {
            Class<?> clazz = result.getClass();

            var getPurchaseId = clazz.getMethod("getId");
            Object purchaseId = getPurchaseId.invoke(result);
            detail.put("purchaseId", purchaseId);

            var getAmount = clazz.getMethod("getPaidAmount");
            Object paidAmount = getAmount.invoke(result);
            detail.put("paidAmount", paidAmount);

            return (Long) purchaseId;

        } catch (Exception e) {
            log.debug("[HistoryLogAspect] 결과에서 구매 정보를 추출하는 데 실패했습니다.: {}", e.getMessage());
            return null;
        }


    }
}
