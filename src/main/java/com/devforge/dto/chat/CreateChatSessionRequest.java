package com.devforge.dto.chat;

import jakarta.validation.constraints.Size;

public record CreateChatSessionRequest(

        @Size(max = 200, message = "Title cannot exceed 200 characters")
        String title

) {
}
