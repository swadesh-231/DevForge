package com.devforge.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    StoredImage upload(MultipartFile file, String fileNamePrefix);
    void delete(String fileId);
    record StoredImage(String url, String fileId) {
    }
}
