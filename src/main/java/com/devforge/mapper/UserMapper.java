package com.devforge.mapper;

import com.devforge.dto.user.UserProfileResponse;
import com.devforge.entity.User;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfiguration.class)
public interface UserMapper {

    UserProfileResponse toProfile(User user);
}
