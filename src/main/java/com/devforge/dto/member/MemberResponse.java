package com.devforge.dto.member;

import com.devforge.entity.ProjectMember;
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
    public static MemberResponse from(ProjectMember member) {
        return new MemberResponse(
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getUser().getName(),
                member.getProjectRole(),
                member.getInvitedAt(),
                member.getAcceptedAt()
        );
    }
}
