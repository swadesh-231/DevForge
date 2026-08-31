package com.devforge.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SafePathValidator implements ConstraintValidator<SafePath, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        if (value.chars().anyMatch(c -> c < 0x20 || c == 0x7F)) {
            return false;
        }
        if (value.indexOf('\\') >= 0) {
            return false;
        }
        if (value.startsWith("/")) {
            return false;
        }
        if (value.length() >= 2 && value.charAt(1) == ':') {
            return false;
        }
        for (String segment : value.split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                return false;
            }
        }
        return true;
    }
}
