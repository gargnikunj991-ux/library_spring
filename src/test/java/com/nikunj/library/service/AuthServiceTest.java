package com.nikunj.library.service;

import com.nikunj.library.dto.LoginRequest;
import com.nikunj.library.dto.LoginResponse;
import com.nikunj.library.dto.RegisterRequest;
import com.nikunj.library.model.RefreshToken;
import com.nikunj.library.model.User;
import com.nikunj.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, authenticationManager, jwtService, refreshTokenService);
    }

    @Test
    @DisplayName("Register User: Successfully registers user with encoded password")
    void testRegisterUser_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("librarian1");
        request.setPassword("plainPassword");
        request.setRole(User.Role.LIBRARIAN);

        when(userRepository.findByUsername("librarian1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");

        String result = authService.registerUser(request);

        assertEquals("User registered successfully", result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register User: Throws exception when username already exists")
    void testRegisterUser_DuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password");
        request.setRole(User.Role.LIBRARIAN);

        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(new User()));

        assertThrows(RuntimeException.class, () -> authService.registerUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login User: Authenticates credentials and returns access & refresh tokens")
    void testLoginUser_Success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("secret");

        User user = new User();
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setRole(User.Role.ADMIN);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generateToken(user)).thenReturn("mocked-jwt-token");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("mocked-refresh-token");
        when(refreshTokenService.createRefreshToken("admin")).thenReturn(refreshToken);

        LoginResponse response = authService.loginUser(request);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getAccessToken());
        assertEquals("mocked-refresh-token", response.getRefreshToken());
    }

    @Test
    @DisplayName("Logout User: Delegates to refreshTokenService")
    void testLogoutUser_Success() {
        when(refreshTokenService.revokeByUsername("admin")).thenReturn("User logged out and refresh token revoked successfully");

        String result = authService.logoutUser("admin");
        assertTrue(result.contains("revoked successfully"));
        verify(refreshTokenService, times(1)).revokeByUsername("admin");
    }
}
