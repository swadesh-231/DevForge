package com.devforge.dto.member;

import com.devforge.entity.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(

        @NotNull(message = "Role is required")
        ProjectRole role

) {
}
