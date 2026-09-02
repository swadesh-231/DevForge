package com.devforge.mapper;

import com.devforge.dto.member.MemberResponse;
import com.devforge.entity.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapperConfiguration.class)
public interface ProjectMemberMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "role", source = "projectRole")
    MemberResponse toMemberResponse(ProjectMember projectMember);

    List<MemberResponse> toMemberResponses(List<ProjectMember> projectMembers);
}
