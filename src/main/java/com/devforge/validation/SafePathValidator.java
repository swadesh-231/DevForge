package com.devforge.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SafePathValidator implements ConstraintValidator<SafePath, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || ProjectFilePaths.isSafe(value);
    }
}
