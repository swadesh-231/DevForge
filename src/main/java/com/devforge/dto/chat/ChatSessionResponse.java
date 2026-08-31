package com.devforge.dto.chat;

import com.devforge.entity.ChatSession;

import java.time.Instant;

public record ChatSessionResponse(
        Long id,
        Long projectId,
        String title,
        Instant createdAt,
        Instant updatedAt
) {
    public static ChatSessionResponse from(ChatSession session) {
        return new ChatSessionResponse(
                session.getId(),
                session.getProject().getId(),
                session.getTitle(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}
