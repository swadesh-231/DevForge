package com.devforge.controller;

import com.devforge.dto.auth.AuthResponse;
import com.devforge.dto.auth.LoginRequest;
import com.devforge.dto.auth.SignupRequest;
import com.devforge.dto.common.ApiResponse;
import com.devforge.security.cookie.RefreshTokenCookieFactory;
import com.devforge.service.AuthService;
import com.devforge.service.AuthService.AuthenticationResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenCookieFactory cookieFactory;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody SignupRequest request) {
        return respond(authService.register(request), HttpStatus.CREATED, "Account created");
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return respond(authService.login(request), HttpStatus.OK, "Logged in");
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @CookieValue(name = "${security.cookie.refresh-token-name}", required = false) String refreshToken) {
        return respond(authService.refresh(refreshToken), HttpStatus.OK, "Token refreshed");
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "${security.cookie.refresh-token-name}", required = false) String refreshToken) {
        authService.logout(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.expire().toString())
                .body(ApiResponse.message("Logged out"));
    }

    private ResponseEntity<ApiResponse<AuthResponse>> respond(
            AuthenticationResult result, HttpStatus status, String message) {
        ResponseCookie cookie = cookieFactory.create(result.refreshToken());
        return ResponseEntity.status(status)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.ok(result.response(), message));
    }
}
