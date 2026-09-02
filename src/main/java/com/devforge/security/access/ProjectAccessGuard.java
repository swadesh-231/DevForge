package com.devforge.security.access;

import com.devforge.entity.Project;
import com.devforge.entity.enums.ProjectPermission;
import com.devforge.exception.ResourceNotFoundException;
import com.devforge.repository.ProjectRepository;
import com.devforge.repository.ProjectRepository.ProjectWithRole;
import com.devforge.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProjectAccessGuard {

    private final ProjectRepository projectRepository;
    private final CurrentUserProvider currentUserProvider;

    public ProjectWithRole require(Long projectId, ProjectPermission permission) {
        return require(projectId, currentUserProvider.requireUserId(), permission);
    }

    public ProjectWithRole require(Long projectId, Long userId, ProjectPermission permission) {
        ProjectWithRole accessible = projectRepository
                .findAccessibleByIdWithRole(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        if (!accessible.getRole().getPermissions().contains(permission)) {
            log.debug("User {} with role {} denied {} on project {}",
                    userId, accessible.getRole(), permission, projectId);
            throw new AccessDeniedException("Missing permission " + permission.getValue());
        }
        return accessible;
    }

    public Project requireProject(Long projectId, ProjectPermission permission) {
        return require(projectId, permission).getProject();
    }

    public Long requireProjectId(Long projectId, ProjectPermission permission) {
        return require(projectId, permission).getProject().getId();
    }
}
