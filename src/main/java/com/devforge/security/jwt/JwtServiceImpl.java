package com.devforge.security.jwt;

import com.devforge.config.JwtProperties;
import com.devforge.config.JwtProperties.TokenProperties;
import com.devforge.entity.User;
import com.devforge.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtServiceImpl implements JwtService {
    private static final String ISSUER = "devforge";
    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_TOKEN_TYPE = "typ";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtProperties jwtProperties;
    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    public JwtServiceImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.accessKey = hmacKey(jwtProperties.accessToken().secretKey());
        this.refreshKey = hmacKey(jwtProperties.refreshToken().secretKey());
    }

    @Override
    public IssuedToken generateAccessToken(User user) {
        return generate(user, jwtProperties.accessToken(), TYPE_ACCESS, accessKey);
    }

    @Override
    public IssuedToken generateRefreshToken(User user) {
        return generate(user, jwtProperties.refreshToken(), TYPE_REFRESH, refreshKey);
    }

    @Override
    public Long extractUserId(String accessToken) {
        return parseAccessClaims(accessToken).get(CLAIM_USER_ID, Long.class);
    }

    @Override
    public String extractEmail(String accessToken) {
        return parseAccessClaims(accessToken).getSubject();
    }

    @Override
    public RefreshTokenClaims parseRefreshToken(String refreshToken) {
        Claims claims = parseClaims(refreshToken, refreshKey, TYPE_REFRESH, "Refresh token");
        return new RefreshTokenClaims(
                claims.get(CLAIM_USER_ID, Long.class),
                claims.getSubject(),
                claims.getId(),
                claims.getExpiration().toInstant());
    }

    private IssuedToken generate(User user, TokenProperties properties, String type, SecretKey key) {
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plus(properties.expiration());

        String value = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(ISSUER)
                .subject(user.getEmail())
                .claim(CLAIM_USER_ID, user.getId())
                .claim(CLAIM_TOKEN_TYPE, type)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();

        return new IssuedToken(value, issuedAt, expiresAt);
    }

    private Claims parseAccessClaims(String token) {
        return parseClaims(token, accessKey, TYPE_ACCESS, "Access token");
    }

    private Claims parseClaims(String token, SecretKey key, String expectedType, String label) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(ISSUER)
                    .require(CLAIM_TOKEN_TYPE, expectedType)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            throw new InvalidTokenException(label + " has expired");
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidTokenException(label + " is invalid");
        }

        if (claims.get(CLAIM_USER_ID, Long.class) == null || claims.getSubject() == null) {
            throw new InvalidTokenException(label + " is invalid");
        }
        return claims;
    }

    private static SecretKey hmacKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
