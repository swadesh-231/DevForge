package com.devforge.dto.chat;

import com.devforge.entity.ChatEvent;
import com.devforge.entity.enums.ChatEventType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRawValue;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatEventResponse(
        Long id,
        ChatEventType type,
        Integer sequenceOrder,
        String content,
        String filePath,
        @JsonRawValue
        String metadata
) {
    public static ChatEventResponse from(ChatEvent event) {
        return new ChatEventResponse(
                event.getId(),
                event.getType(),
                event.getSequenceOrder(),
                event.getContent(),
                event.getFilePath(),
                event.getMetadata()
        );
    }
}
