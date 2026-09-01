package com.devforge.service;

public interface UsageRecordService {
    void recordTokenUsage(Long userId, int actualTokens);
    void checkDailyTokensUsage();
}
