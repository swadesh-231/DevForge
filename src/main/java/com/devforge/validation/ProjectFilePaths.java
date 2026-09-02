package com.devforge.validation;

import com.devforge.exception.InvalidFileException;

public final class ProjectFilePaths {

    public static final int MAX_LENGTH = 512;

    private ProjectFilePaths() {
    }

    public static boolean isSafe(String path) {
        if (path == null || path.isBlank() || path.length() > MAX_LENGTH) {
            return false;
        }
        if (path.chars().anyMatch(character -> character < 0x20 || character == 0x7F)) {
            return false;
        }
        if (path.indexOf('\\') >= 0 || path.startsWith("/")) {
            return false;
        }
        if (path.length() >= 2 && path.charAt(1) == ':') {
            return false;
        }
        for (String segment : path.split("/", -1)) {
            if (segment.isEmpty() || segment.equals(".") || segment.equals("..")) {
                return false;
            }
        }
        return true;
    }

    public static String normalize(String path) {
        String candidate = path == null ? null : path.trim();
        if (candidate != null && candidate.startsWith("./")) {
            candidate = candidate.substring(2);
        }
        while (candidate != null && candidate.startsWith("/")) {
            candidate = candidate.substring(1);
        }
        if (!isSafe(candidate)) {
            throw new InvalidFileException("Invalid file path: " + path);
        }
        return candidate;
    }
}
