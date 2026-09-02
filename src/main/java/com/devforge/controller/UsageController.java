package com.devforge.controller;

import com.devforge.dto.billing.UsageTodayResponse;
import com.devforge.dto.common.ApiResponse;
import com.devforge.security.principal.UserPrincipal;
import com.devforge.service.UsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UsageController {

    private final UsageService usageService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<UsageTodayResponse>> getTodayUsage(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.ok(usageService.getTodayUsage(principal.getId())));
    }
}
