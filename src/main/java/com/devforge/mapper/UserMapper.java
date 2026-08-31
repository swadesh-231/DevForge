package com.devforge.mapper;

import com.devforge.dto.user.UserProfileResponse;
import com.devforge.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserProfileResponse toProfile(User user);
}
