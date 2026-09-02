package com.devforge.service;

import com.devforge.dto.project.CreateProjectRequest;
import com.devforge.dto.project.ProjectResponse;
import com.devforge.dto.project.ProjectSummaryResponse;
import com.devforge.dto.project.UpdateProjectRequest;

import java.util.List;

public interface ProjectService {

    List<ProjectSummaryResponse> getUserProjects();

    ProjectSummaryResponse getUserProjectById(Long projectId);

    ProjectResponse createProject(CreateProjectRequest request);

    ProjectResponse updateProject(Long projectId, UpdateProjectRequest request);

    void softDelete(Long projectId);
}
