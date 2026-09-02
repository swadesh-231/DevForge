package com.devforge.service;

import com.devforge.dto.billing.UsageTodayResponse;

public interface UsageService {

    void recordUsage(Long userId, int tokensIn, int tokensOut);

    void assertWithinDailyTokenLimit(Long userId);

    UsageTodayResponse getTodayUsage(Long userId);
}
