package com.devforge.service.impl;

import com.devforge.dto.project.ProjectRequest;
import com.devforge.dto.project.ProjectResponse;
import com.devforge.dto.project.ProjectSummaryResponse;
import com.devforge.entity.Project;
import com.devforge.entity.ProjectMember;
import com.devforge.entity.ProjectMemberId;
import com.devforge.entity.User;
import com.devforge.entity.enums.ProjectPermission;
import com.devforge.entity.enums.ProjectRole;
import com.devforge.exception.BadRequestException;
import com.devforge.exception.ResourceNotFoundException;
import com.devforge.mapper.ProjectMapper;
import com.devforge.repository.ProjectMemberRepository;
import com.devforge.repository.ProjectRepository;
import com.devforge.repository.ProjectRepository.ProjectWithRole;
import com.devforge.repository.UserRepository;
import com.devforge.security.principal.UserPrincipal;
import com.devforge.service.ProjectService;
import com.devforge.service.ProjectTemplateService;
import com.devforge.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService {

    private static final Pattern NON_SLUG_CHARS = Pattern.compile("[^a-z0-9]+");
    private static final Pattern LEADING_TRAILING_DASHES = Pattern.compile("^-+|-+$");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final String FALLBACK_SLUG = "project";
    private static final int MAX_SLUG_BASE_LENGTH = 120;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final SubscriptionService subscriptionService;
    private final ProjectTemplateService projectTemplateService;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        if (!subscriptionService.canCreateNewProject()) {
            throw new BadRequestException("PROJECT_LIMIT_REACHED",
                    "Your current plan does not allow another project. Upgrade your plan to continue.");
        }

        Long userId = currentUserId();
        User owner = userRepository.findById(userId)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        String name = request.name().trim();
        Project project = projectRepository.save(Project.builder()
                .owner(owner)
                .name(name)
                .slug(generateSlug(name))
                .description(normalizeDescription(request.description()))
                .isPublic(request.publicOrDefault())
                .build());

        projectMemberRepository.save(ProjectMember.builder()
                .id(new ProjectMemberId(project.getId(), owner.getId()))
                .project(project)
                .user(owner)
                .projectRole(ProjectRole.OWNER)
                .acceptedAt(Instant.now())
                .build());

        projectTemplateService.initializeProjectFromTemplate(project.getId());

        log.info("Created project {} ({}) for user {}", project.getId(), project.getSlug(), userId);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public List<ProjectSummaryResponse> getUserProjects() {
        return projectRepository.findAllAccessibleByUser(currentUserId()).stream()
                .map(accessible -> projectMapper.toProjectSummaryResponse(
                        accessible.getProject(), accessible.getRole()))
                .toList();
    }

    @Override
    public ProjectSummaryResponse getUserProjectById(Long projectId) {
        ProjectWithRole accessible = requireAccess(projectId, ProjectPermission.VIEW);
        return projectMapper.toProjectSummaryResponse(accessible.getProject(), accessible.getRole());
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        Project project = requireAccess(projectId, ProjectPermission.EDIT).getProject();

        // PATCH semantics: the name is always supplied (@NotBlank), the rest only when present.
        project.setName(request.name().trim());
        if (request.description() != null) {
            project.setDescription(normalizeDescription(request.description()));
        }
        if (request.isPublic() != null) {
            project.setIsPublic(request.isPublic());
        }

        // The slug is part of the project's public URL, so it stays fixed across renames.
        return projectMapper.toProjectResponse(project);
    }

    @Override
    @Transactional
    public void softDelete(Long projectId) {
        Project project = requireAccess(projectId, ProjectPermission.DELETE).getProject();

        project.setDeletedAt(Instant.now());
        log.info("Soft deleted project {} by user {}", projectId, currentUserId());
    }

    ///  INTERNAL FUNCTIONS

    /**
     * Loads a project the caller is a member of together with the caller's role, and asserts that the
     * role grants {@code permission}. A non-member gets a 404 rather than a 403 so that project
     * existence is not leaked.
     */
    private ProjectWithRole requireAccess(Long projectId, ProjectPermission permission) {
        Long userId = currentUserId();
        ProjectWithRole accessible = projectRepository
                .findAccessibleProjectByIdWithRole(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        if (!accessible.getRole().getPermissions().contains(permission)) {
            log.debug("User {} with role {} denied {} on project {}",
                    userId, accessible.getRole(), permission, projectId);
            throw new AccessDeniedException("Missing permission " + permission.getValue());
        }
        return accessible;
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new AuthenticationCredentialsNotFoundException("No authenticated user found");
        }
        return principal.getId();
    }

    /**
     * Builds a URL-safe slug from the project name and appends random entropy, which keeps the
     * unique slug column collision-free without an extra lookup-and-retry round trip.
     */
    private static String generateSlug(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFKD);
        String base = DIACRITICS.matcher(normalized).replaceAll("");
        base = NON_SLUG_CHARS.matcher(base.toLowerCase(Locale.ROOT)).replaceAll("-");
        base = LEADING_TRAILING_DASHES.matcher(base).replaceAll("");

        if (base.length() > MAX_SLUG_BASE_LENGTH) {
            base = LEADING_TRAILING_DASHES.matcher(base.substring(0, MAX_SLUG_BASE_LENGTH)).replaceAll("");
        }
        if (base.isEmpty()) {
            base = FALLBACK_SLUG;
        }

        byte[] entropy = new byte[5];
        RANDOM.nextBytes(entropy);
        return base + "-" + HexFormat.of().formatHex(entropy);
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
