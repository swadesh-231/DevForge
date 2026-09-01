package com.devforge.service;

import com.devforge.dto.project.ProjectRequest;
import com.devforge.dto.project.ProjectResponse;
import com.devforge.dto.project.ProjectSummaryResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectSummaryResponse> getUserProjects();
    ProjectSummaryResponse getUserProjectById(Long id);
    ProjectResponse createProject(ProjectRequest request);
    ProjectResponse updateProject(Long id, ProjectRequest request);
    void softDelete(Long id);
}
