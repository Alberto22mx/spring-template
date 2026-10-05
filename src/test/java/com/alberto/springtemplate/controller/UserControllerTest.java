package com.alberto.springtemplate.controller;

import com.alberto.springtemplate.dto.request.UserRequest;
import com.alberto.springtemplate.dto.response.UserResponse;
import com.alberto.springtemplate.entity.Role;
import com.alberto.springtemplate.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void findByIdReturnsUser() throws Exception {
        UserResponse response = new UserResponse(1L, "Alice", "alice@example.com", Role.USER);
        when(userService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/users/{id}", 1L).with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        verify(userService).findById(1L);
    }

    @Test
    void createReturnsCreatedUser() throws Exception {
        UserRequest request = new UserRequest("Alice", "alice@example.com", "Password123!");
        UserResponse response = new UserResponse(1L, "Alice", "alice@example.com", Role.USER);
        when(userService.create(request)).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).create(request);
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/{id}", 1L)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    @Test
    void createReturnsForbiddenForRegularUser() throws Exception {
        UserRequest request = new UserRequest("Alice", "alice@example.com", "Password123!");

        mockMvc.perform(post("/api/users")
                        .with(user("user").roles("USER"))
                        .with(csrf())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
