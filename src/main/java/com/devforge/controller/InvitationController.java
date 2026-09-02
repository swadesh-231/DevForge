package com.devforge.controller;

import com.devforge.dto.common.ApiResponse;
import com.devforge.dto.member.InvitationResponse;
import com.devforge.dto.member.MemberResponse;
import com.devforge.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final ProjectMemberService projectMemberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvitationResponse>>> getMyInvitations() {
        return ResponseEntity.ok(ApiResponse.ok(projectMemberService.getMyInvitations()));
    }

    @PostMapping("/{projectId}/accept")
    public ResponseEntity<ApiResponse<MemberResponse>> acceptInvitation(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.ok(
                projectMemberService.acceptInvitation(projectId), "Invitation accepted"));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Void>> declineInvitation(@PathVariable Long projectId) {
        projectMemberService.declineInvitation(projectId);
        return ResponseEntity.ok(ApiResponse.message("Invitation declined"));
    }
}
