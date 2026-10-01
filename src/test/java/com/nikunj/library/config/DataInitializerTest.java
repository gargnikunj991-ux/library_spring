package com.nikunj.library.config;

import com.nikunj.library.model.User;
import com.nikunj.library.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("initAdminUser: Seeds admin user when not present")
    void testInitAdminUser_WhenNotPresent() throws Exception {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("admin123")).thenReturn("encodedPassword");

        DataInitializer initializer = new DataInitializer();
        CommandLineRunner runner = initializer.initAdminUser(userRepository, passwordEncoder, "admin", "admin123");
        runner.run();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        User savedUser = captor.getValue();
        assertEquals("admin", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals("ROLE_ADMIN", savedUser.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    @DisplayName("initAdminUser: Skips seeding when admin already exists")
    void testInitAdminUser_WhenAlreadyPresent() throws Exception {
        User existingAdmin = new User();
        existingAdmin.setUsername("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(existingAdmin));

        DataInitializer initializer = new DataInitializer();
        CommandLineRunner runner = initializer.initAdminUser(userRepository, passwordEncoder, "admin", "admin123");
        runner.run();

        verify(userRepository, never()).save(any());
    }
}
