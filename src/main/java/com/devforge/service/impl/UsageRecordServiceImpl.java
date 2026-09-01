package com.devforge.service.impl;

import com.devforge.service.UsageRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsageRecordServiceImpl implements UsageRecordService {

    @Override
    public void recordTokenUsage(Long userId, int actualTokens) {

    }

    @Override
    public void checkDailyTokensUsage() {

    }
}
