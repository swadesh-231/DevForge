package com.devforge.dto.file;

import com.devforge.validation.SafePath;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertFileRequest(

        @NotBlank(message = "Path is required")
        @Size(max = 512, message = "Path cannot exceed 512 characters")
        @SafePath
        String path,

        @NotNull(message = "Content is required")
        @Size(max = 1_048_576, message = "File content cannot exceed 1 MB")
        String content

) {
}
