package com.devforge.service.impl;

import com.devforge.dto.member.InviteMemberRequest;
import com.devforge.dto.member.MemberResponse;
import com.devforge.dto.member.UpdateMemberRoleRequest;
import com.devforge.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Transactional
public class ProjectMemberServiceImpl implements ProjectMemberService {