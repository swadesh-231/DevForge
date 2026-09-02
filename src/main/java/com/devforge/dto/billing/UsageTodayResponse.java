package com.devforge.dto.billing;

import java.time.LocalDate;

public record UsageTodayResponse(
        LocalDate date,
        Integer tokensIn,
        Integer tokensOut,
        Integer tokensUsed,
        Integer tokensLimit,
        Boolean unlimited,
        Integer messageCount,
        Integer previewSeconds
) {
}
