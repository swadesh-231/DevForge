package com.devforge.service.impl;

import com.devforge.dto.project.CreateProjectRequest;
import com.devforge.dto.project.ProjectResponse;
import com.devforge.dto.project.ProjectSummaryResponse;
import com.devforge.dto.project.UpdateProjectRequest;
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
import com.devforge.repository.UserRepository;
import com.devforge.security.CurrentUserProvider;
import com.devforge.security.access.ProjectAccessGuard;
import com.devforge.service.ProjectService;
import com.devforge.service.ProjectTemplateService;
import com.devforge.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ProjectAccessGuard projectAccessGuard;
    private final CurrentUserProvider currentUserProvider;
    private final SubscriptionService subscriptionService;
    private final ProjectTemplateService projectTemplateService;

    @Override
    public List<ProjectSummaryResponse> getUserProjects() {
        return projectMapper.toProjectSummaryResponses(
                projectRepository.findAllAccessibleByUser(currentUserProvider.requireUserId()));
    }

    @Override
    public ProjectSummaryResponse getUserProjectById(Long projectId) {
        return projectMapper.toProjectSummaryResponse(
                projectAccessGuard.require(projectId, ProjectPermission.VIEW));
    }

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        Long userId = currentUserProvider.requireUserId();

        if (!subscriptionService.canCreateProject(userId)) {
            throw new BadRequestException("PROJECT_LIMIT_REACHED",
                    "Your current plan does not allow another project. Upgrade your plan to continue.");
        }

        User owner = userRepository.findByIdAndDeletedAtIsNull(userId)
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
    @Transactional
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request) {
        Project project = projectAccessGuard.requireProject(projectId, ProjectPermission.EDIT);

        if (request.name() != null) {
            project.setName(request.name().trim());
        }
        if (request.description() != null) {
            project.setDescription(normalizeDescription(request.description()));
        }
        if (request.isPublic() != null) {
            project.setIsPublic(request.isPublic());
        }

        return projectMapper.toProjectResponse(project);
    }

    @Override
    @Transactional
    public void softDelete(Long projectId) {
        Project project = projectAccessGuard.requireProject(projectId, ProjectPermission.DELETE);

        project.setDeletedAt(Instant.now());
        log.info("Soft deleted project {} by user {}", projectId, currentUserProvider.requireUserId());
    }

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
