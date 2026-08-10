package com.nikunj.library.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nikunj.library.dto.LoginResponse;
import com.nikunj.library.dto.RefreshTokenRequest;
import com.nikunj.library.exception.TokenRefreshException;
import com.nikunj.library.model.RefreshToken;
import com.nikunj.library.model.User;
import com.nikunj.library.repository.RefreshTokenRepository;
import com.nikunj.library.repository.UserRepository;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final Long refreshExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                                UserRepository userRepository,
                                JwtService jwtService,
                                @Value("${jwt.refresh-expiration-ms:604800000}") Long refreshExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        // Clean up previous refresh tokens for this user
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isRevoked() || token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token has expired or was revoked. Please log in again.");
        }
        return token;
    }

    @Transactional
    public LoginResponse refreshAccessToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .map(this::verifyExpiration)
                .orElseThrow(() -> new TokenRefreshException(request.getRefreshToken(), "Refresh token is not found in database!"));

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user);

        return new LoginResponse(newAccessToken, refreshToken.getToken());
    }

    @Transactional
    public String revokeByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        refreshTokenRepository.revokeByUser(user);
        return "User logged out and refresh token revoked successfully";
    }

    @Transactional
    public int deleteByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return refreshTokenRepository.deleteByUser(user);
    }
}
