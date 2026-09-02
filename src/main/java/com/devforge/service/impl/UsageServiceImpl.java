package com.devforge.service.impl;

import com.devforge.dto.billing.PlanLimitsResponse;
import com.devforge.dto.billing.UsageTodayResponse;
import com.devforge.entity.UsageRecord;
import com.devforge.exception.QuotaExceededException;
import com.devforge.repository.UsageRecordRepository;
import com.devforge.service.SubscriptionService;
import com.devforge.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsageServiceImpl implements UsageService {

    private final UsageRecordRepository usageRecordRepository;
    private final SubscriptionService subscriptionService;

    @Override
    @Transactional
    public void recordUsage(Long userId, int tokensIn, int tokensOut) {
        usageRecordRepository.addUsage(
                userId, LocalDate.now(), Math.max(tokensIn, 0), Math.max(tokensOut, 0), 1);
    }

    @Override
    public void assertWithinDailyTokenLimit(Long userId) {
        PlanLimitsResponse limits = subscriptionService.getEffectiveLimits(userId);
        if (Boolean.TRUE.equals(limits.unlimitedAi())) {
            return;
        }

        int used = tokensUsedToday(userId);
        if (used >= limits.maxTokensPerDay()) {
            throw new QuotaExceededException(
                    "Daily token limit reached for the " + limits.planName() + " plan. Upgrade to continue.");
        }
    }

    @Override
    public UsageTodayResponse getTodayUsage(Long userId) {
        PlanLimitsResponse limits = subscriptionService.getEffectiveLimits(userId);
        Optional<UsageRecord> today = usageRecordRepository.findByUserIdAndDate(userId, LocalDate.now());

        int tokensIn = today.map(UsageRecord::getTokensIn).orElse(0);
        int tokensOut = today.map(UsageRecord::getTokensOut).orElse(0);

        return new UsageTodayResponse(
                LocalDate.now(),
                tokensIn,
                tokensOut,
                tokensIn + tokensOut,
                limits.maxTokensPerDay(),
                limits.unlimitedAi(),
                today.map(UsageRecord::getMessageCount).orElse(0),
                today.map(UsageRecord::getPreviewSeconds).orElse(0));
    }

    private int tokensUsedToday(Long userId) {
        return usageRecordRepository.findByUserIdAndDate(userId, LocalDate.now())
                .map(record -> record.getTokensIn() + record.getTokensOut())
                .orElse(0);
    }
}
