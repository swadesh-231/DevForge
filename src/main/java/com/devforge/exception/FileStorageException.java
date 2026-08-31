package com.devforge.exception;

import org.springframework.http.HttpStatus;

public class FileStorageException extends ApiException {
    public FileStorageException(String message) {
        super(HttpStatus.BAD_GATEWAY, "FILE_STORAGE_ERROR", message);
    }
}
