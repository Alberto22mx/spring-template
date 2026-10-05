package com.alberto.springtemplate.service.impl;

import com.alberto.springtemplate.dto.request.UserRequest;
import com.alberto.springtemplate.dto.request.UpdateUserRequest;
import com.alberto.springtemplate.dto.response.UserResponse;
import com.alberto.springtemplate.entity.Role;
import com.alberto.springtemplate.entity.User;
import com.alberto.springtemplate.exception.DuplicateEmailException;
import com.alberto.springtemplate.exception.ResourceNotFoundException;
import com.alberto.springtemplate.mapper.UserMapper;
import com.alberto.springtemplate.repository.UserRepository;
import com.alberto.springtemplate.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userMapper.toResponse(getUser(id));
    }

    @Override
    public UserResponse create(UserRequest request) {
        validateEmailAvailability(request.email());
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse update(Long id, UpdateUserRequest request) {
        User user = getUser(id);
        if (!user.getEmail().equals(request.email())) {
            validateEmailAvailability(request.email());
        }
        user.setName(request.name());
        user.setEmail(request.email());
        if (StringUtils.hasText(request.password())) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(getUser(id));
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private void validateEmailAvailability(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }
}
