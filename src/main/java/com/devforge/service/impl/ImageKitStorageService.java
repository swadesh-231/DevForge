package com.devforge.service.impl;

import com.devforge.config.ImageKitProperties;
import com.devforge.exception.FileStorageException;
import com.devforge.exception.InvalidFileException;
import com.devforge.service.ImageStorageService;
import io.imagekit.client.ImageKitClient;
import io.imagekit.models.files.FileUploadParams;
import io.imagekit.models.files.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageKitStorageService implements ImageStorageService {
    private final ImageKitClient imageKitClient;
    private final ImageKitProperties imageKitProperties;

    @Override
    public StoredImage upload(MultipartFile file, String fileNamePrefix) {
        validate(file);
        try (InputStream stream = file.getInputStream()) {
            FileUploadParams params = FileUploadParams.builder()
                    .file(stream)
                    .fileName(fileNamePrefix + "-" + UUID.randomUUID())
                    .folder(imageKitProperties.avatarFolder())
                    .useUniqueFileName(true)
                    .build();

            FileUploadResponse response = imageKitClient.files().upload(params);

            String url = response.url()
                    .orElseThrow(() -> new FileStorageException("Image host returned no URL"));
            String fileId = response.fileId()
                    .orElseThrow(() -> new FileStorageException("Image host returned no file id"));

            return new StoredImage(url, fileId);
        } catch (IOException exception) {
            throw new FileStorageException("Could not read the uploaded image");
        } catch (RuntimeException exception) {
            log.error("ImageKit upload failed", exception);
            throw new FileStorageException("Image upload failed");
        }
    }

    @Override
    public void delete(String fileId) {
        try {
            imageKitClient.files().delete(fileId);
        } catch (RuntimeException exception) {
            log.warn("ImageKit delete failed for fileId {}", fileId, exception);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Image file is required");
        }
        if (file.getSize() > imageKitProperties.maxAvatarBytes()) {
            throw new InvalidFileException(
                    "Image must be smaller than " + imageKitProperties.maxAvatarBytes() / 1024 + " KB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !imageKitProperties.allowedContentTypes().contains(contentType)) {
            throw new InvalidFileException("Unsupported image type. Allowed: "
                    + String.join(", ", imageKitProperties.allowedContentTypes()));
        }
    }
}
