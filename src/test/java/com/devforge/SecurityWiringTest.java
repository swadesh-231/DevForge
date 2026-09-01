package com.devforge;

import com.devforge.config.ImageKitProperties;
import com.devforge.dto.auth.AuthResponse;
import com.devforge.dto.user.UserProfileResponse;
import com.devforge.entity.enums.UserRole;
import com.devforge.repository.RefreshTokenRepository;
import com.devforge.repository.UserRepository;
import com.devforge.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class SecurityWiringTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockitoBean
    private AuthService authService;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void contextLoadsWithSecurityBeans() {
        assertThat(context.getBean("securityFilterChain")).isNotNull();
        assertThat(context.getBean(com.devforge.security.filters.JwtAuthFilter.class)).isNotNull();
        assertThat(context.getBean(com.devforge.controller.AuthController.class)).isNotNull();
        assertThat(context.getBean(com.devforge.controller.UserController.class)).isNotNull();
        assertThat(context.getBean(com.devforge.exception.GlobalExceptionHandler.class)).isNotNull();
        assertThat(context.getBean(io.imagekit.client.ImageKitClient.class)).isNotNull();
        assertThat(context.getBean(com.devforge.mapper.UserMapper.class)).isNotNull();
    }

    @Test
    void imageKitPropertiesBindFromTheAppNamespace() {
        ImageKitProperties properties = context.getBean(ImageKitProperties.class);

        assertThat(properties.publicKey()).isEqualTo("public_test_key");
        assertThat(properties.privateKey()).isEqualTo("private_test_key");
        assertThat(properties.urlEndpoint()).isEqualTo("https://ik.imagekit.io/test");
        assertThat(properties.avatarFolder()).isEqualTo("/devforge/avatars");
        assertThat(properties.maxAvatarBytes()).isEqualTo(2_097_152L);
        assertThat(properties.allowedContentTypes()).contains("image/jpeg", "image/png", "image/webp");
    }

    @Test
    void protectedEndpointReturnsStructuredUnauthorized() throws Exception {
        mockMvc().perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.error.path").value("/api/users/me"));
    }

    @Test
    void invalidSignupReturnsFieldErrors() throws Exception {
        mockMvc().perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nope\",\"name\":\"A\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.error.fieldErrors").isArray());
    }

    @Test
    void registerIssuesHttpOnlyRefreshCookie() throws Exception {
        UserProfileResponse profile = new UserProfileResponse(
                1L, "dev@example.com", "Swadesh", null, UserRole.USER, Instant.now());
        AuthResponse response = AuthResponse.builder()
                .accessToken("signed.jwt.token")
                .tokenType("Bearer")
                .expiresAt(Instant.now().plusSeconds(900))
                .user(profile)
                .build();
        when(authService.register(any()))
                .thenReturn(new AuthService.AuthenticationResult(response, "raw-refresh-token"));

        mockMvc().perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"dev@example.com\",\"name\":\"Swadesh\",\"password\":\"hunter2pass\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("signed.jwt.token"))
                .andExpect(jsonPath("$.data.user.email").value("dev@example.com"))
                .andExpect(jsonPath("$.data.user.password").doesNotExist())
                .andExpect(header().string("Set-Cookie",
                        org.hamcrest.Matchers.allOf(
                                org.hamcrest.Matchers.containsString("devforge_refresh_token=raw-refresh-token"),
                                org.hamcrest.Matchers.containsString("HttpOnly"),
                                org.hamcrest.Matchers.containsString("Secure"),
                                org.hamcrest.Matchers.containsString("SameSite=Lax"),
                                org.hamcrest.Matchers.containsString("Path=/api/auth"))));
    }

    @Test
    void logoutClearsRefreshCookie() throws Exception {
        mockMvc().perform(post("/api/auth/logout").cookie(
                        new jakarta.servlet.http.Cookie("devforge_refresh_token", "raw-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie",
                        org.hamcrest.Matchers.containsString("Max-Age=0")));
    }
}
