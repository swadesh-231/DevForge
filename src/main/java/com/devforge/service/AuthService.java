package com.devforge.service;

import com.devforge.dto.auth.AuthResponse;
import com.devforge.dto.auth.LoginRequest;
import com.devforge.dto.auth.SignupRequest;

public interface AuthService {
    AuthenticationResult register(SignupRequest request);

    AuthenticationResult login(LoginRequest request);

    AuthenticationResult refresh(String refreshToken);

    void logout(String refreshToken);

    record AuthenticationResult(AuthResponse response, String refreshToken) {
    }
}
