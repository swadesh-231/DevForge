package com.devforge.dto.member;

import com.devforge.entity.enums.ProjectRole;

import java.time.Instant;

public record MemberResponse(
        Long userId,
        String email,
        String name,
        ProjectRole role,
        Instant invitedAt,
        Instant acceptedAt
) {
}
