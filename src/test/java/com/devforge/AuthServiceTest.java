package com.devforge;

import com.devforge.config.JwtProperties;
import com.devforge.dto.auth.LoginRequest;
import com.devforge.dto.auth.SignupRequest;
import com.devforge.entity.RefreshToken;
import com.devforge.entity.User;
import com.devforge.entity.enums.UserRole;
import com.devforge.exception.DuplicateResourceException;
import com.devforge.exception.InvalidCredentialsException;
import com.devforge.exception.InvalidTokenException;
import com.devforge.mapper.UserMapperImpl;
import com.devforge.repository.RefreshTokenRepository;
import com.devforge.repository.UserRepository;
import com.devforge.security.jwt.JwtServiceImpl;
import com.devforge.service.AuthService.AuthenticationResult;
import com.devforge.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoder passwordEncoder;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();

        JwtProperties jwtProperties = new JwtProperties(
                "test-secret-that-is-definitely-long-enough-32",
                "devforge",
                Duration.ofMinutes(15),
                Duration.ofDays(7));

        authService = new AuthServiceImpl(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                new JwtServiceImpl(jwtProperties),
                jwtProperties,
                new UserMapperImpl());

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private User persistedUser(String rawPassword) {
        return User.builder()
                .id(1L)
                .name("Swadesh")
                .email("dev@example.com")
                .password(passwordEncoder.encode(rawPassword))
                .role(UserRole.USER)
                .build();
    }

    @Test
    void registerNormalisesEmailAndHashesPassword() {
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        AuthenticationResult result = authService.register(
                new SignupRequest("  Dev@Example.COM ", " Swadesh ", "hunter2pass"));

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("dev@example.com")
                        && user.getName().equals("Swadesh")
                        && !user.getPassword().equals("hunter2pass")
                        && user.getPassword().startsWith("$2")));

        assertThat(result.response().accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.response().user().email()).isEqualTo("dev@example.com");
    }

    @Test
    void registerRejectsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new SignupRequest("dev@example.com", "Swadesh", "hunter2pass")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginRejectsWrongPasswordWithoutRevealingWhichFieldFailed() {
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("dev@example.com"))
                .thenReturn(Optional.of(persistedUser("hunter2pass")));

        assertThatThrownBy(() -> authService.login(new LoginRequest("dev@example.com", "wrongpass1")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void loginRejectsUnknownEmailWithIdenticalMessage() {
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(anyString()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", "hunter2pass")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void refreshRotatesTokenAndRevokesThePresentedOne() {
        User user = persistedUser("hunter2pass");
        RefreshToken stored = RefreshToken.builder()
                .id(10L)
                .user(user)
                .tokenHash("hash")
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .build();
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));

        AuthenticationResult result = authService.refresh("some-raw-token");

        assertThat(stored.getRevokedAt()).isNotNull();
        assertThat(result.refreshToken()).isNotEqualTo("some-raw-token");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void reusingRevokedTokenRevokesEveryTokenForThatUser() {
        User user = persistedUser("hunter2pass");
        RefreshToken revoked = RefreshToken.builder()
                .id(10L)
                .user(user)
                .tokenHash("hash")
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .revokedAt(Instant.now().minusSeconds(60))
                .build();
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> authService.refresh("stolen-token"))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenRepository).revokeAllByUserId(eq(1L), any(Instant.class));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void expiredRefreshTokenIsRejected() {
        User user = persistedUser("hunter2pass");
        RefreshToken expired = RefreshToken.builder()
                .id(10L)
                .user(user)
                .tokenHash("hash")
                .expiresAt(Instant.now().minusSeconds(60))
                .build();
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh("expired-token"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void unknownRefreshTokenIsRejected() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("garbage"))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refreshTokenIsStoredHashedNotInPlaintext() {
        when(userRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        AuthenticationResult result = authService.register(
                new SignupRequest("dev@example.com", "Swadesh", "hunter2pass"));

        verify(refreshTokenRepository).save(argThat(token ->
                !token.getTokenHash().equals(result.refreshToken())
                        && token.getTokenHash().length() == 64
                        && token.getTokenHash().matches("[0-9a-f]{64}")));
    }
}
