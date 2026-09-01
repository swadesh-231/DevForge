package com.devforge.service.impl;

import com.devforge.dto.member.InviteMemberRequest;
import com.devforge.dto.member.MemberResponse;
import com.devforge.dto.member.UpdateMemberRoleRequest;
import com.devforge.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {
    @Override
    public List<MemberResponse> getProjectMembers(Long projectId) {
        return List.of();
    }

    @Override
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request) {
        return null;
    }

    @Override
    public MemberResponse updateMemberRole(Long projectId, Long memberId, UpdateMemberRoleRequest request) {
        return null;
    }

    @Override
    public void removeProjectMember(Long projectId, Long memberId) {

    }
}
