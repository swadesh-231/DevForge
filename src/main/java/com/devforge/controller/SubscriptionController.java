package com.devforge.controller;

import com.devforge.dto.billing.PlanLimitsResponse;
import com.devforge.dto.billing.SubscriptionResponse;
import com.devforge.dto.common.ApiResponse;
import com.devforge.security.principal.UserPrincipal;
import com.devforge.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getCurrentSubscription(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                subscriptionService.getCurrentSubscription(principal.getId()).orElse(null)));
    }

    @GetMapping("/me/limits")
    public ResponseEntity<ApiResponse<PlanLimitsResponse>> getCurrentLimits(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(subscriptionService.getEffectiveLimits(principal.getId())));
    }
}
