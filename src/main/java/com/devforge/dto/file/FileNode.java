package com.devforge.dto.file;

import com.devforge.entity.ProjectFile;

import java.time.Instant;


public record FileNode(
        Long id,
        String path,
        Long sizeBytes,
        String mimeType,
        Instant updatedAt
) {
    public static FileNode from(ProjectFile file) {
        return new FileNode(
                file.getId(),
                file.getPath(),
                file.getSizeBytes(),
                file.getMimeType(),
                file.getUpdatedAt()
        );
    }
}
