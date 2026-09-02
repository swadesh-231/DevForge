package com.devforge.exception;

import org.springframework.http.HttpStatus;

public class InvalidFileException extends ApiException {
    public InvalidFileException(String message) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, "INVALID_FILE", message);
    }
}
