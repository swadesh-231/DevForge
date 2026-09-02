package com.devforge.dto.chat;

import java.time.Instant;

public record ChatSessionResponse(
        Long id,
        Long projectId,
        String title,
        Instant createdAt,
        Instant updatedAt
) {
}
