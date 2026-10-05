package com.alberto.springtemplate.config;

import com.alberto.springtemplate.entity.Role;
import com.alberto.springtemplate.entity.User;
import com.alberto.springtemplate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initializeAdministrator() {
        return args -> {
            if (!userRepository.existsByEmail("admin@example.com")) {
                userRepository.save(User.builder()
                        .name("Administrator")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("Admin123!"))
                        .role(Role.ADMIN)
                        .build());
            }
        };
    }
}
