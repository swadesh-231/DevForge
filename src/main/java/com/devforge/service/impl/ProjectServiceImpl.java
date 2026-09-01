package com.devforge.service.impl;

import com.devforge.dto.project.ProjectRequest;
import com.devforge.dto.project.ProjectResponse;
import com.devforge.dto.project.ProjectSummaryResponse;
import com.devforge.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    @Override
    public List<ProjectSummaryResponse> getUserProjects() {
        return List.of();
    }

    @Override
    public ProjectSummaryResponse getUserProjectById(Long id) {
        return null;
    }

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        return null;
    }

    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        return null;
    }

    @Override
    public void softDelete(Long id) {

    }
}
