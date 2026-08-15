package com.nikunj.library.service;

import com.nikunj.library.dto.LoginResponse;
import com.nikunj.library.dto.RefreshTokenRequest;
import com.nikunj.library.exception.TokenRefreshException;
import com.nikunj.library.model.RefreshToken;
import com.nikunj.library.model.User;
import com.nikunj.library.repository.RefreshTokenRepository;
import com.nikunj.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private RefreshTokenService refreshTokenService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, userRepository, jwtService, 604800000L);

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("admin");
        sampleUser.setPassword("encodedPassword");
        sampleUser.setRole(User.Role.ADMIN);
    }

    @Test
    @DisplayName("Create Refresh Token: Successfully cleans up previous token and saves new token")
    void testCreateRefreshToken_Success() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(sampleUser));

        RefreshToken token = new RefreshToken();
        token.setId(1L);
        token.setUser(sampleUser);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(Instant.now().plusMillis(604800000L));
        token.setRevoked(false);

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(token);

        RefreshToken createdToken = refreshTokenService.createRefreshToken("admin");

        assertNotNull(createdToken);
        assertEquals(sampleUser, createdToken.getUser());
        assertFalse(createdToken.isRevoked());

        verify(refreshTokenRepository, times(1)).deleteByUser(sampleUser);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Verify Expiration: Valid unexpired and unrevoked token passes verification")
    void testVerifyExpiration_ValidToken() {
        RefreshToken token = new RefreshToken();
        token.setToken("valid-uuid-token");
        token.setExpiryDate(Instant.now().plusSeconds(3600));
        token.setRevoked(false);

        RefreshToken verified = refreshTokenService.verifyExpiration(token);
        assertEquals(token, verified);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Verify Expiration: Expired token gets deleted and throws TokenRefreshException")
    void testVerifyExpiration_ExpiredToken_ThrowsException() {
        RefreshToken token = new RefreshToken();
        token.setToken("expired-uuid-token");
        token.setExpiryDate(Instant.now().minusSeconds(3600));
        token.setRevoked(false);

        assertThrows(TokenRefreshException.class, () -> refreshTokenService.verifyExpiration(token));
        verify(refreshTokenRepository, times(1)).delete(token);
    }

    @Test
    @DisplayName("Verify Expiration: Revoked token gets deleted and throws TokenRefreshException")
    void testVerifyExpiration_RevokedToken_ThrowsException() {
        RefreshToken token = new RefreshToken();
        token.setToken("revoked-uuid-token");
        token.setExpiryDate(Instant.now().plusSeconds(3600));
        token.setRevoked(true);

        assertThrows(TokenRefreshException.class, () -> refreshTokenService.verifyExpiration(token));
        verify(refreshTokenRepository, times(1)).delete(token);
    }

    @Test
    @DisplayName("Refresh Access Token: Successfully issues fresh access token")
    void testRefreshAccessToken_Success() {
        RefreshToken token = new RefreshToken();
        token.setToken("valid-token-str");
        token.setUser(sampleUser);
        token.setExpiryDate(Instant.now().plusSeconds(3600));
        token.setRevoked(false);

        when(refreshTokenRepository.findByToken("valid-token-str")).thenReturn(Optional.of(token));
        when(jwtService.generateToken(sampleUser)).thenReturn("new-access-jwt-token");

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-token-str");

        LoginResponse response = refreshTokenService.refreshAccessToken(request);

        assertNotNull(response);
        assertEquals("new-access-jwt-token", response.getAccessToken());
        assertEquals("valid-token-str", response.getRefreshToken());
    }

    @Test
    @DisplayName("Revoke by Username: Successfully revokes active refresh token")
    void testRevokeByUsername_Success() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(sampleUser));
        when(refreshTokenRepository.revokeByUser(sampleUser)).thenReturn(1);

        String result = refreshTokenService.revokeByUsername("admin");
        assertTrue(result.contains("revoked successfully"));
        verify(refreshTokenRepository, times(1)).revokeByUser(sampleUser);
    }
}
