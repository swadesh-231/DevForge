package com.devforge.service.impl;

import com.devforge.service.UsageRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

private final UsageLogRepository usageLogRepository;
private final AuthUtil authUtil;
private final SubscriptionService subscriptionService;

@Override
public void recordTokenUsage(Long userId, int actualTokens) {
    LocalDate today = LocalDate.now();

    UsageLog todayLog = usageLogRepository.findByUserIdAndDate(userId, today).
            orElseGet(() -> createNewDailyLog(userId, today));

    todayLog.setTokensUsed(todayLog.getTokensUsed() + actualTokens);
    usageLogRepository.save(todayLog);
}

@Override
public void checkDailyTokensUsage() {
    Long userId = authUtil.getCurrentUserId();
    SubscriptionResponse subscriptionResponse = subscriptionService.getCurrentSubscription();
    PlanResponse plan = subscriptionResponse.plan();

    LocalDate today = LocalDate.now();

    UsageLog todayLog = usageLogRepository.findByUserIdAndDate(userId, today).
            orElseGet(() -> createNewDailyLog(userId, today));

    if(plan.unlimitedAi()) return;

    int currentUsage = todayLog.getTokensUsed();
    int limit = plan.maxTokensPerDay();

    if(currentUsage >=  limit) {
        throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Daily limit reached, Upgrade now");
    }

}

private UsageLog createNewDailyLog(Long userId, LocalDate date) {
    UsageLog newLog = UsageLog.builder()
            .userId(userId)
            .date(date)
            .tokensUsed(0)
            .build();
    return usageLogRepository.save(newLog);
}