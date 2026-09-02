package com.devforge.controller;

import com.devforge.dto.common.ApiResponse;
import com.devforge.dto.file.FileContentResponse;
import com.devforge.dto.file.FileTreeResponse;
import com.devforge.dto.file.UpsertFileRequest;
import com.devforge.entity.enums.ProjectPermission;
import com.devforge.security.access.ProjectAccessGuard;
import com.devforge.service.ProjectFileService;
import com.devforge.validation.SafePath;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/projects/{projectId}/files")
@RequiredArgsConstructor
public class ProjectFileController {

    private final ProjectFileService projectFileService;
    private final ProjectAccessGuard projectAccessGuard;

    @GetMapping
    public ResponseEntity<ApiResponse<FileTreeResponse>> getFileTree(@PathVariable Long projectId) {
        projectAccessGuard.require(projectId, ProjectPermission.VIEW);
        return ResponseEntity.ok(ApiResponse.ok(projectFileService.getFileTree(projectId)));
    }

    @GetMapping("/content")
    public ResponseEntity<ApiResponse<FileContentResponse>> getFileContent(
            @PathVariable Long projectId,
            @RequestParam @NotBlank @SafePath String path) {
        projectAccessGuard.require(projectId, ProjectPermission.VIEW);
        return ResponseEntity.ok(ApiResponse.ok(projectFileService.getFileContent(projectId, path)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<FileContentResponse>> upsertFile(
            @PathVariable Long projectId,
            @Valid @RequestBody UpsertFileRequest request) {
        projectAccessGuard.require(projectId, ProjectPermission.EDIT);
        return ResponseEntity.ok(ApiResponse.ok(
                projectFileService.saveFile(projectId, request.path(), request.content()), "File saved"));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable Long projectId,
            @RequestParam @NotBlank @SafePath String path) {
        projectAccessGuard.require(projectId, ProjectPermission.EDIT);
        projectFileService.deleteFile(projectId, path);
        return ResponseEntity.ok(ApiResponse.message("File deleted"));
    }
}
