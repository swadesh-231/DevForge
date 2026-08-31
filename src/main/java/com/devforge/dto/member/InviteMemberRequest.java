package com.devforge.dto.member;

import com.devforge.entity.enums.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InviteMemberRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        @Size(max = 255, message = "Email cannot exceed 255 characters")
        String email,

        @NotNull(message = "Role is required")
        ProjectRole role

) {
}
