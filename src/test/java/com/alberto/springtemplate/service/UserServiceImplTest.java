package com.alberto.springtemplate.service;

import com.alberto.springtemplate.dto.request.UserRequest;
import com.alberto.springtemplate.dto.request.UpdateUserRequest;
import com.alberto.springtemplate.dto.response.UserResponse;
import com.alberto.springtemplate.entity.Role;
import com.alberto.springtemplate.entity.User;
import com.alberto.springtemplate.exception.ResourceNotFoundException;
import com.alberto.springtemplate.mapper.UserMapper;
import com.alberto.springtemplate.repository.UserRepository;
import com.alberto.springtemplate.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse response;
    private UserRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Alice").email("alice@example.com")
                .password("encoded-password").role(Role.USER).build();
        response = new UserResponse(1L, "Alice", "alice@example.com", Role.USER);
        request = new UserRequest("Alice", "alice@example.com", "Password123!");
    }

    @Test
    void findAllReturnsMappedUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        List<UserResponse> result = userService.findAll();

        assertEquals(1, result.size());
        assertEquals(response, result.getFirst());
        verify(userRepository).findAll();
    }

    @Test
    void findByIdReturnsUserWhenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.findById(1L);

        assertEquals(response, result);
        verify(userRepository).findById(1L);
    }

    @Test
    void findByIdThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findById(99L));
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void createSavesEncodedPassword() {
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.create(request);

        assertNotNull(result);
        assertEquals(response, result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateChangesUserAndSavesIt() {
        UpdateUserRequest updateRequest = new UpdateUserRequest("Alice Updated", "new@example.com", "NewPassword123!");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(updateRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(updateRequest.password())).thenReturn("new-encoded-password");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.update(1L, updateRequest);

        assertEquals(response, result);
        assertEquals("Alice Updated", user.getName());
        assertEquals("new@example.com", user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void deleteRemovesExistingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(userRepository).delete(user);
        verify(userRepository).findById(1L);
    }
}
