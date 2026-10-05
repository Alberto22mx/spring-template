package com.alberto.springtemplate.mapper;

import com.alberto.springtemplate.dto.response.UserResponse;
import com.alberto.springtemplate.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
