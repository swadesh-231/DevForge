package com.devforge.scheduling;

import com.devforge.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupTask {

    private static final Duration RETENTION = Duration.ofDays(7);

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        int purged = refreshTokenRepository.deleteExpiredBefore(Instant.now().minus(RETENTION));
        if (purged > 0) {
            log.info("Purged {} expired refresh tokens", purged);
        }
    }
}
