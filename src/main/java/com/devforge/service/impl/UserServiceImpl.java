package com.devforge.service.impl;

import com.devforge.dto.user.UpdateProfileRequest;
import com.devforge.dto.user.UserProfileResponse;
import com.devforge.entity.User;
import com.devforge.exception.ResourceNotFoundException;
import com.devforge.mapper.UserMapper;
import com.devforge.repository.UserRepository;
import com.devforge.service.ImageStorageService;
import com.devforge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(Long userId) {
        return userMapper.toProfile(requireUser(userId));
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = requireUser(userId);
        if (request.name() != null) {
            user.setName(request.name().trim());
        }
        return userMapper.toProfile(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfileImage(Long userId, MultipartFile image) {
        User user = requireUser(userId);
        String previousFileId = user.getImageFileId();

        ImageStorageService.StoredImage stored = imageStorageService.upload(image, "user-" + userId);
        user.setImageUrl(stored.url());
        user.setImageFileId(stored.fileId());

        if (previousFileId != null) {
            imageStorageService.delete(previousFileId);
        }
        return userMapper.toProfile(user);
    }

    @Override
    @Transactional
    public UserProfileResponse removeProfileImage(Long userId) {
        User user = requireUser(userId);
        String fileId = user.getImageFileId();

        user.setImageUrl(null);
        user.setImageFileId(null);

        if (fileId != null) {
            imageStorageService.delete(fileId);
        }
        return userMapper.toProfile(user);
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
