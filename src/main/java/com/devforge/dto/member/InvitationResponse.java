package com.devforge.dto.member;

import com.devforge.entity.enums.ProjectRole;

import java.time.Instant;

public record InvitationResponse(
        Long projectId,
        String projectName,
        String projectSlug,
        ProjectRole role,
        Instant invitedAt
) {
}
