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
    void testSaveAndFindByUsername() {
        User user = new User("johndoe", "hashed_password", "CUSTOMER");
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("johndoe");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("johndoe");
        assertThat(found.get().getPassword()).isEqualTo("hashed_password");
        assertThat(found.get().getRole()).isEqualTo("CUSTOMER");
        assertThat(found.get().getId()).isNotNull();
    }

    @Test
    void testExistsByUsername() {
        User user = new User("janedoe", "hashed_password", "ADMIN");
        userRepository.save(user);

        assertThat(userRepository.existsByUsername("janedoe")).isTrue();
        assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
    }
}
