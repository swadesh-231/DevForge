package com.devforge.service.impl;

import com.devforge.dto.member.InvitationResponse;
import com.devforge.dto.member.InviteMemberRequest;
import com.devforge.dto.member.MemberResponse;
import com.devforge.dto.member.UpdateMemberRoleRequest;
import com.devforge.entity.Project;
import com.devforge.entity.ProjectMember;
import com.devforge.entity.ProjectMemberId;
import com.devforge.entity.User;
import com.devforge.entity.enums.ProjectPermission;
import com.devforge.entity.enums.ProjectRole;
import com.devforge.exception.BadRequestException;
import com.devforge.exception.DuplicateResourceException;
import com.devforge.exception.ResourceNotFoundException;
import com.devforge.mapper.ProjectMemberMapper;
import com.devforge.repository.ProjectMemberRepository;
import com.devforge.repository.UserRepository;
import com.devforge.security.CurrentUserProvider;
import com.devforge.security.access.ProjectAccessGuard;
import com.devforge.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectAccessGuard projectAccessGuard;
    private final CurrentUserProvider currentUserProvider;

    @Override
    public List<MemberResponse> getProjectMembers(Long projectId) {
        projectAccessGuard.require(projectId, ProjectPermission.VIEW_MEMBERS);
        return projectMemberMapper.toMemberResponses(
                projectMemberRepository.findByProjectIdWithUser(projectId));
    }

    @Override
    @Transactional
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request) {
        Project project = projectAccessGuard.requireProject(projectId, ProjectPermission.MANAGE_MEMBERS);
        requireAssignableRole(request.role());

        String email = request.email().trim().toLowerCase();
        User invitee = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        if (projectMemberRepository.existsByIdProjectIdAndIdUserId(projectId, invitee.getId())) {
            throw new DuplicateResourceException("This user is already a member of the project");
        }

        ProjectMember member = projectMemberRepository.save(ProjectMember.builder()
                .id(new ProjectMemberId(projectId, invitee.getId()))
                .project(project)
                .user(invitee)
                .projectRole(request.role())
                .build());

        log.info("Invited user {} to project {} as {}", invitee.getId(), projectId, request.role());
        return projectMemberMapper.toMemberResponse(member);
    }

    @Override
    @Transactional
    public MemberResponse updateMemberRole(Long projectId, Long userId, UpdateMemberRoleRequest request) {
        projectAccessGuard.require(projectId, ProjectPermission.MANAGE_MEMBERS);
        requireAssignableRole(request.role());

        ProjectMember member = requireMember(projectId, userId);
        if (member.getProjectRole() == ProjectRole.OWNER) {
            throw new BadRequestException("OWNER_ROLE_IMMUTABLE", "The project owner's role cannot be changed");
        }

        member.setProjectRole(request.role());
        return projectMemberMapper.toMemberResponse(member);
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long userId) {
        projectAccessGuard.require(projectId, ProjectPermission.MANAGE_MEMBERS);

        ProjectMember member = requireMember(projectId, userId);
        if (member.getProjectRole() == ProjectRole.OWNER) {
            throw new BadRequestException("OWNER_NOT_REMOVABLE", "The project owner cannot be removed");
        }

        projectMemberRepository.delete(member);
        log.info("Removed user {} from project {}", userId, projectId);
    }

    @Override
    public List<InvitationResponse> getMyInvitations() {
        return projectMemberMapper.toInvitationResponses(
                projectMemberRepository.findPendingInvitationsByUserId(currentUserProvider.requireUserId()));
    }

    @Override
    @Transactional
    public MemberResponse acceptInvitation(Long projectId) {
        ProjectMember invitation = requirePendingInvitation(projectId);
        invitation.setAcceptedAt(Instant.now());
        return projectMemberMapper.toMemberResponse(invitation);
    }

    @Override
    @Transactional
    public void declineInvitation(Long projectId) {
        projectMemberRepository.delete(requirePendingInvitation(projectId));
    }

    private ProjectMember requireMember(Long projectId, Long userId) {
        return projectMemberRepository.findByIdProjectIdAndIdUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project member", userId));
    }

    private ProjectMember requirePendingInvitation(Long projectId) {
        ProjectMember member = requireMember(projectId, currentUserProvider.requireUserId());
        if (member.getAcceptedAt() != null) {
            throw new BadRequestException("INVITATION_ALREADY_ACCEPTED", "This invitation was already accepted");
        }
        return member;
    }

    private static void requireAssignableRole(ProjectRole role) {
        if (role == ProjectRole.OWNER) {
            throw new BadRequestException("OWNER_ROLE_NOT_ASSIGNABLE",
                    "The owner role cannot be assigned; transfer ownership instead");
        }
    }
}
