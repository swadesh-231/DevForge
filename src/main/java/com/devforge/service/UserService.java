package com.devforge.service;

import com.devforge.dto.user.UpdateProfileRequest;
import com.devforge.dto.user.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserProfileResponse getCurrentUser(Long userId);

    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

    UserProfileResponse updateProfileImage(Long userId, MultipartFile image);

    UserProfileResponse removeProfileImage(Long userId);
}
