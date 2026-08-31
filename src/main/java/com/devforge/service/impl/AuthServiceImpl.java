package com.devforge.service.impl;

import com.devforge.config.JwtProperties;
import com.devforge.dto.auth.AuthResponse;
import com.devforge.dto.auth.LoginRequest;
import com.devforge.dto.auth.SignupRequest;
import com.devforge.entity.RefreshToken;
import com.devforge.entity.User;
import com.devforge.entity.enums.UserRole;
import com.devforge.exception.DuplicateResourceException;
import com.devforge.exception.InvalidCredentialsException;
import com.devforge.exception.InvalidTokenException;
import com.devforge.mapper.UserMapper;
import com.devforge.repository.RefreshTokenRepository;
import com.devforge.repository.UserRepository;
import com.devforge.security.jwt.JwtService;
import com.devforge.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final int REFRESH_TOKEN_BYTES = 64;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserMapper userMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public AuthenticationResult register(SignupRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = userRepository.save(User.builder()
                .name(request.name().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .build());

        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthenticationResult login(LoginRequest request) {
        User user = userRepository
                .findByEmailIgnoreCaseAndDeletedAtIsNull(request.email().trim().toLowerCase())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthenticationResult refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidTokenException("Refresh token is missing");
        }

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid"));

        Instant now = Instant.now();
        if (!stored.isActive(now)) {
            refreshTokenRepository.revokeAllByUserId(stored.getUser().getId(), now);
            log.warn("Reuse of an inactive refresh token for user {}", stored.getUser().getId());
            throw new InvalidTokenException("Refresh token is no longer valid");
        }

        stored.setRevokedAt(now);
        return issueTokens(stored.getUser());
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }
        refreshTokenRepository.findByTokenHash(hash(refreshToken))
                .filter(token -> token.getRevokedAt() == null)
                .ifPresent(token -> token.setRevokedAt(Instant.now()));
    }

    private AuthenticationResult issueTokens(User user) {
        Instant now = Instant.now();
        String rawRefreshToken = generateRefreshToken();

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hash(rawRefreshToken))
                .expiresAt(now.plus(jwtProperties.refreshTokenTtl()))
                .build());

        AuthResponse response = AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .tokenType("Bearer")
                .expiresAt(now.plus(jwtProperties.accessTokenTtl()))
                .user(userMapper.toProfile(user))
                .build();

        return new AuthenticationResult(response, rawRefreshToken);
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
