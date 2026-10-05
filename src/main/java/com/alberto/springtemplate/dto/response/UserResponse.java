package com.alberto.springtemplate.dto.response;

import com.alberto.springtemplate.entity.Role;

public record UserResponse(Long id, String name, String email, Role role) {
}
