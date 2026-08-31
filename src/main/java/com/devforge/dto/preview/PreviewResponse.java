package com.devforge.dto.preview;

import com.devforge.entity.Preview;
import com.devforge.entity.enums.PreviewStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PreviewResponse(
        Long id,
        Long projectId,
        String previewUrl,
        PreviewStatus status,
        String errorMessage,
        Instant startedAt,
        Instant endedAt,
        Instant lastAccessedAt
) {
    public static PreviewResponse from(Preview preview) {
        return new PreviewResponse(
                preview.getId(),
                preview.getProject().getId(),
                preview.getPreviewUrl(),
                preview.getStatus(),
                preview.getErrorMessage(),
                preview.getStartedAt(),
                preview.getEndedAt(),
                preview.getLastAccessedAt()
        );
    }
}
