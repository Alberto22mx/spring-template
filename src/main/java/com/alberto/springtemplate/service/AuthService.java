package com.alberto.springtemplate.service;

import com.alberto.springtemplate.dto.request.LoginRequest;
import com.alberto.springtemplate.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);
}
