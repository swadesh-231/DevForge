package com.devforge.dto.file;

import java.time.Instant;

public record FileContentResponse(
        Long id,
        String path,
        String content,
        String contentHash,
        Long sizeBytes,
        String mimeType,
        Instant updatedAt
) {
}
