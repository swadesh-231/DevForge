package com.devforge.dto.file;

import java.util.List;

public record FileTreeResponse(Long projectId, List<FileNode> files) {
}
