package com.alberto.springtemplate.repository;

import com.alberto.springtemplate.entity.Role;
import com.alberto.springtemplate.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveFindByIdFindByEmailAndExistsByEmailWork() {
        User user = userRepository.save(User.builder()
                .name("Alice")
                .email("alice@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build());

        assertNotNull(user.getId());
        assertTrue(userRepository.findById(user.getId()).isPresent());
        assertTrue(userRepository.findByEmail("alice@example.com").isPresent());
        assertTrue(userRepository.existsByEmail("alice@example.com"));
        assertFalse(userRepository.existsByEmail("missing@example.com"));
    }
}
