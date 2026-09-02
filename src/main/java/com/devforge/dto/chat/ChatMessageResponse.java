package com.devforge.dto.chat;

import com.devforge.entity.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatMessageResponse(
        Long id,
        Long sessionId,
        MessageRole role,
        Integer sequenceOrder,
        String content,
        List<ChatEventResponse> events,
        Integer tokensIn,
        Integer tokensOut,
        Instant createdAt
) {
}
