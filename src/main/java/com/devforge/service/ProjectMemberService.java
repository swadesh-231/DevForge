package com.devforge.service;

import com.devforge.dto.member.InvitationResponse;
import com.devforge.dto.member.InviteMemberRequest;
import com.devforge.dto.member.MemberResponse;
import com.devforge.dto.member.UpdateMemberRoleRequest;

import java.util.List;

public interface ProjectMemberService {

    List<MemberResponse> getProjectMembers(Long projectId);

    MemberResponse inviteMember(Long projectId, InviteMemberRequest request);

    MemberResponse updateMemberRole(Long projectId, Long userId, UpdateMemberRoleRequest request);

    void removeMember(Long projectId, Long userId);

    List<InvitationResponse> getMyInvitations();

    MemberResponse acceptInvitation(Long projectId);

    void declineInvitation(Long projectId);
}
