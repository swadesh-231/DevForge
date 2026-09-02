package com.devforge.dto.chat;

import com.devforge.entity.enums.ChatEventType;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StreamResponse(
        ChatEventType type,
        Long sessionId,
        String text,
        String error,
        Boolean done
) {
    public static StreamResponse delta(String text) {
        return new StreamResponse(ChatEventType.MESSAGE, null, text, null, false);
    }

    public static StreamResponse started(Long sessionId) {
        return new StreamResponse(null, sessionId, null, null, false);
    }

    public static StreamResponse failed(String error) {
        return new StreamResponse(null, null, null, error, true);
    }

    public static StreamResponse complete() {
        return new StreamResponse(null, null, null, null, true);
    }
}
