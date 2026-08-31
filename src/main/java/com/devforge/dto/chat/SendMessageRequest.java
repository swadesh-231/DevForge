package com.devforge.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(

        @NotBlank(message = "Message content is required")
        @Size(max = 32_000, message = "Message cannot exceed 32000 characters")
        String content

) {
}
