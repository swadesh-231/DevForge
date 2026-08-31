package com.devforge.dto.billing;

public record UsageTodayResponse(
        Integer tokensIn,
        Integer tokensOut,
        Integer tokensUsed,
        Integer tokensLimit,
        Integer messageCount,
        Integer previewsRunning,
        Integer previewsLimit
) {
}
