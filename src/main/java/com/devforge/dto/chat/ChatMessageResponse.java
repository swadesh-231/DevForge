package com.devforge.dto.chat;

import com.devforge.entity.ChatMessage;
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
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getChatSession().getId(),
                message.getRole(),
                message.getSequenceOrder(),
                message.getContent(),
                message.getEvents().stream().map(ChatEventResponse::from).toList(),
                message.getTokensIn(),
                message.getTokensOut(),
                message.getCreatedAt()
        );
    }
}
