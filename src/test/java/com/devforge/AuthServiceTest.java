package com.devforge;

import com.devforge.config.JwtProperties;
import com.devforge.config.JwtProperties.TokenProperties;
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
    private JwtServiceImpl jwtService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();

        JwtProperties jwtProperties = new JwtProperties(
                new TokenProperties("test-access-secret-that-is-long-enough-32", Duration.ofMinutes(15)),
                new TokenProperties("test-refresh-secret-that-is-long-enough-32", Duration.ofDays(7)));
        jwtService = new JwtServiceImpl(jwtProperties);

        authService = new AuthServiceImpl(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtService,
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
    void bothIssuedTokensAreSignedJwtsWithTheirOwnSecret() {
        User user = persistedUser("hunter2pass");
        String accessToken = jwtService.generateAccessToken(user).value();
        String refreshToken = jwtService.generateRefreshToken(user).value();

        assertThat(accessToken.split("\\.")).hasSize(3);
        assertThat(refreshToken.split("\\.")).hasSize(3);
        assertThat(accessToken).isNotEqualTo(refreshToken);

        assertThat(jwtService.extractUserId(accessToken)).isEqualTo(1L);
        assertThat(jwtService.extractEmail(accessToken)).isEqualTo("dev@example.com");
        assertThat(jwtService.parseRefreshToken(refreshToken).userId()).isEqualTo(1L);
    }

    @Test
    void accessTokenIsNotAcceptedAsARefreshToken() {
        String accessToken = jwtService.generateAccessToken(persistedUser("hunter2pass")).value();

        assertThatThrownBy(() -> jwtService.parseRefreshToken(accessToken))
                .isInstanceOf(InvalidTokenException.class);
        verify(refreshTokenRepository, never()).findByTokenHash(anyString());
    }

    @Test
    void refreshTokenIsNotAcceptedAsAnAccessToken() {
        String refreshToken = jwtService.generateRefreshToken(persistedUser("hunter2pass")).value();

        assertThatThrownBy(() -> jwtService.extractEmail(refreshToken))
                .isInstanceOf(InvalidTokenException.class);
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
        String presented = jwtService.generateRefreshToken(user).value();
        RefreshToken stored = RefreshToken.builder()
                .id(10L)
                .user(user)
                .tokenHash("hash")
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .build();
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));

        AuthenticationResult result = authService.refresh(presented);

        assertThat(stored.getRevokedAt()).isNotNull();
        assertThat(result.refreshToken()).isNotEqualTo(presented);
        assertThat(jwtService.parseRefreshToken(result.refreshToken()).userId()).isEqualTo(1L);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void reusingRevokedTokenRevokesEveryTokenForThatUser() {
        User user = persistedUser("hunter2pass");
        String stolen = jwtService.generateRefreshToken(user).value();
        RefreshToken revoked = RefreshToken.builder()
                .id(10L)
                .user(user)
                .tokenHash("hash")
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .revokedAt(Instant.now().minusSeconds(60))
                .build();
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> authService.refresh(stolen))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenRepository).revokeAllByUserId(eq(1L), any(Instant.class));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void expiredRefreshTokenIsRejected() {
        User user = persistedUser("hunter2pass");
        String presented = jwtService.generateRefreshToken(user).value();
        RefreshToken expired = RefreshToken.builder()
                .id(10L)
                .user(user)
                .tokenHash("hash")
                .expiresAt(Instant.now().minusSeconds(60))
                .build();
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> authService.refresh(presented))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void tamperedRefreshTokenIsRejectedBeforeAnyDatabaseLookup() {
        assertThatThrownBy(() -> authService.refresh("garbage"))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenRepository, never()).findByTokenHash(anyString());
    }

    @Test
    void validlySignedTokenThatIsNotOnRecordRevokesTheUsersSessions() {
        String orphan = jwtService.generateRefreshToken(persistedUser("hunter2pass")).value();
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(orphan))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenRepository).revokeAllByUserId(eq(1L), any(Instant.class));
        verify(refreshTokenRepository, never()).save(any());
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

    @Test
    void logoutRevokesTheStoredToken() {
        User user = persistedUser("hunter2pass");
        String presented = jwtService.generateRefreshToken(user).value();
        RefreshToken stored = RefreshToken.builder()
                .id(10L)
                .user(user)
                .tokenHash("hash")
                .expiresAt(Instant.now().plus(Duration.ofDays(1)))
                .build();
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(stored));

        authService.logout(presented);

        assertThat(stored.getRevokedAt()).isNotNull();
    }
}
