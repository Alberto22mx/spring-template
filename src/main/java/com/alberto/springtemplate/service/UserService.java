package com.alberto.springtemplate.service;

import com.alberto.springtemplate.dto.request.UserRequest;
import com.alberto.springtemplate.dto.request.UpdateUserRequest;
import com.alberto.springtemplate.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    List<UserResponse> findAll();

    Page<UserResponse> findAll(Pageable pageable);

    UserResponse findById(Long id);

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UpdateUserRequest request);

    void delete(Long id);
}
