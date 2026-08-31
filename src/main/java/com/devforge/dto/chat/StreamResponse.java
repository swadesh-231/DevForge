package com.devforge.dto.chat;

import com.devforge.entity.enums.ChatEventType;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StreamResponse(
        ChatEventType type,
        Integer sequenceOrder,
        String text,
        String filePath,
        Boolean done
) {
    public static StreamResponse delta(ChatEventType type, int sequenceOrder, String text) {
        return new StreamResponse(type, sequenceOrder, text, null, false);
    }

    public static StreamResponse complete() {
        return new StreamResponse(null, null, null, null, true);
    }
}
