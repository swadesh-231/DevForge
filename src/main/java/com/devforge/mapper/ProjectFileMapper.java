package com.devforge.mapper;

import com.devforge.dto.file.FileContentResponse;
import com.devforge.dto.file.FileNode;
import com.devforge.entity.ProjectFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapperConfiguration.class)
public interface ProjectFileMapper {

    FileNode toFileNode(ProjectFile projectFile);

    List<FileNode> toFileNodes(List<ProjectFile> projectFiles);

    @Mapping(target = "id", source = "file.id")
    @Mapping(target = "path", source = "file.path")
    @Mapping(target = "contentHash", source = "file.contentHash")
    @Mapping(target = "sizeBytes", source = "file.sizeBytes")
    @Mapping(target = "mimeType", source = "file.mimeType")
    @Mapping(target = "updatedAt", source = "file.updatedAt")
    @Mapping(target = "content", source = "content")
    FileContentResponse toFileContentResponse(ProjectFile file, String content);
}
