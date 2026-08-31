package com.devforge.dto.project;

import jakarta.validation.constraints.Size;

public record UpdateProjectRequest(

        @Size(min = 1, max = 100, message = "Project name must be between 1 and 100 characters")
        String name,

        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        String description,

        Boolean isPublic

) {
}
