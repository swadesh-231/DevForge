package com.devforge.mapper;

import com.devforge.dto.project.ProjectResponse;
import com.devforge.dto.project.ProjectSummaryResponse;
import com.devforge.entity.Project;
import com.devforge.repository.ProjectRepository.ProjectWithRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapperConfiguration.class, uses = UserMapper.class)
public interface ProjectMapper {

    ProjectResponse toProjectResponse(Project project);

    @Mapping(target = "id", source = "project.id")
    @Mapping(target = "name", source = "project.name")
    @Mapping(target = "slug", source = "project.slug")
    @Mapping(target = "isPublic", source = "project.isPublic")
    @Mapping(target = "createdAt", source = "project.createdAt")
    @Mapping(target = "updatedAt", source = "project.updatedAt")
    @Mapping(target = "role", source = "role")
    ProjectSummaryResponse toProjectSummaryResponse(ProjectWithRole projectWithRole);

    List<ProjectSummaryResponse> toProjectSummaryResponses(List<ProjectWithRole> projects);
}
