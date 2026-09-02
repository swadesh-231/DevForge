package com.devforge.exception;

import org.springframework.http.HttpStatus;

public class QuotaExceededException extends ApiException {

    public QuotaExceededException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, "QUOTA_EXCEEDED", message);
    }
}
