package com.devforge.dto.chat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(

        @NotNull(message = "Project id is required")
        @Positive(message = "Project id must be positive")
        Long projectId,

        Long sessionId,

        @NotBlank(message = "Message content is required")
        @Size(max = 32_000, message = "Message cannot exceed 32000 characters")
        String content

) {
}
