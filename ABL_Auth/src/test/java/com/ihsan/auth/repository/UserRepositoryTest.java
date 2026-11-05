package com.ihsan.auth.repository;

import com.ihsan.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindByEmail() {
        User user = new User("johndoe@example.com", "hashed_password", "CUSTOMER");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("johndoe@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("johndoe@example.com");
        assertThat(found.get().getPassword()).isEqualTo("hashed_password");
        assertThat(found.get().getRole()).isEqualTo("CUSTOMER");
        assertThat(found.get().getId()).isNotNull();
    }

    @Test
    void testExistsByEmail() {
        User user = new User("janedoe@example.com", "hashed_password", "ADMIN");
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("janedoe@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }
}
