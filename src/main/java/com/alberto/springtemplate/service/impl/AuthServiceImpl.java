package com.alberto.springtemplate.service.impl;

import com.alberto.springtemplate.dto.request.LoginRequest;
import com.alberto.springtemplate.dto.response.AuthResponse;
import com.alberto.springtemplate.entity.User;
import com.alberto.springtemplate.exception.InvalidCredentialsException;
import com.alberto.springtemplate.repository.UserRepository;
import com.alberto.springtemplate.security.JwtService;
import com.alberto.springtemplate.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return new AuthResponse(jwtService.generateToken(user));
    }
}
