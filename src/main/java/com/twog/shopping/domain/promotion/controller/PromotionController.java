package com.twog.shopping.domain.promotion.controller;

import com.twog.shopping.domain.promotion.dto.CampaignRequestDto;
import com.twog.shopping.domain.promotion.dto.CampaignResponseDto;
import com.twog.shopping.domain.promotion.service.PromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/promotion")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    // 캠페인 생성 (ADMIN)
    @PostMapping("/campaigns")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampaignResponseDto> createCampaign(@RequestBody CampaignRequestDto requestDto) {
        return ResponseEntity.ok(promotionService.createCampaign(requestDto));
    }

    // 캠페인 수정 (ADMIN)
    @PutMapping("/campaigns/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampaignResponseDto> updateCampaign(
            @PathVariable Long id,
            @RequestBody CampaignRequestDto requestDto) {
        return ResponseEntity.ok(promotionService.updateCampaign(id, requestDto));
    }

    // 캠페인 삭제 (ADMIN)
    @DeleteMapping("/campaigns/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        promotionService.deleteCampaign(id);
        return ResponseEntity.ok().build();
    }

    // 이메일 발송 배치 트리거 (ADMIN)
    @PostMapping("/campaigns/{id}/send")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> executeCampaign(@PathVariable Long id) {
        promotionService.executeCampaign(id);
        return ResponseEntity.ok("캠페인 발송 배치가 성공적으로 실행되었습니다.");
    }

    // 이메일 클릭 추적 (USER/PUBLIC)
    @GetMapping("/click/{sendId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> trackEmailClick(@PathVariable Long sendId) {
        promotionService.trackEmailClick(sendId);
        return ResponseEntity.ok("이메일 확인이 처리되었습니다.");
    }
}
