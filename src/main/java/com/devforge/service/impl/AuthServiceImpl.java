package com.devforge.service.impl;

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
import com.devforge.security.jwt.JwtService.IssuedToken;
import com.devforge.security.jwt.JwtService.RefreshTokenClaims;
import com.devforge.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

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

        RefreshTokenClaims claims = jwtService.parseRefreshToken(refreshToken);
        Instant now = Instant.now();

        Optional<RefreshToken> found = refreshTokenRepository.findByTokenHash(hash(refreshToken));
        if (found.isEmpty()) {
            log.warn("Refresh token signed for user {} is not on record", claims.userId());
            refreshTokenRepository.revokeAllByUserId(claims.userId(), now);
            throw new InvalidTokenException("Refresh token is invalid");
        }

        RefreshToken stored = found.get();
        User user = stored.getUser();

        if (!user.getId().equals(claims.userId())) {
            log.warn("Refresh token claim/owner mismatch: claimed {}, stored {}", claims.userId(), user.getId());
            throw new InvalidTokenException("Refresh token is invalid");
        }

        if (!stored.isActive(now)) {
            log.warn("Reuse of an inactive refresh token for user {}", user.getId());
            refreshTokenRepository.revokeAllByUserId(user.getId(), now);
            throw new InvalidTokenException("Refresh token is no longer valid");
        }

        if (user.getDeletedAt() != null) {
            refreshTokenRepository.revokeAllByUserId(user.getId(), now);
            throw new InvalidTokenException("Account is no longer active");
        }

        stored.setRevokedAt(now);
        return issueTokens(user);
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
        IssuedToken refreshToken = jwtService.generateRefreshToken(user);
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hash(refreshToken.value()))
                .expiresAt(refreshToken.expiresAt())
                .build());

        IssuedToken accessToken = jwtService.generateAccessToken(user);
        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken.value())
                .tokenType(TOKEN_TYPE)
                .expiresAt(accessToken.expiresAt())
                .user(userMapper.toProfile(user))
                .build();

        return new AuthenticationResult(response, refreshToken.value());
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
