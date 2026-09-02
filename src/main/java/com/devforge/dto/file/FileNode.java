package com.devforge.dto.file;

import java.time.Instant;

public record FileNode(
        Long id,
        String path,
        Long sizeBytes,
        String mimeType,
        Instant updatedAt
) {
}
