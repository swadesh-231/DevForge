package com.devforge.mapper;

import com.devforge.dto.project.ProjectResponse;
import com.devforge.entity.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface ProjectMapper {

    ProjectResponse toResponse(Project project);
}
