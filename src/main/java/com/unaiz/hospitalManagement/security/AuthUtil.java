package com.unaiz.hospitalManagement.security;

import com.unaiz.hospitalManagement.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class AuthUtil {

    // Define the secret key property via application.properties or application.yml
    @Value("${jwt.secret.key}")
    private String jwtSecreteKey;

    // 24 hours in milliseconds (using L for long literal)
    final long expirationTimeMs = 24 * 60 * 60 * 1000L;

    /**
     * Retrieves the HMAC secret key derived from the base64-encoded secret string.
     */
    private SecretKey getSecretKey() {
        // NOTE: If jwtSecreteKey is a Base64-encoded string, you might need to decode it first.
        // Assuming it's a regular string, we convert it to bytes.
        return Keys.hmacShaKeyFor(jwtSecreteKey.getBytes(StandardCharsets.UTF_8));
    }


    /**
     * Generates a new JWT Access Token for the given user.
     */
    public String generateAccessToken(User user) {
        return Jwts.builder()
                // Set the 'sub' (subject) claim to the username
                .subject(user.getUsername())
                .claim("userId", user.getId().toString())
                .issuedAt(new Date())
                // Set the 'exp' (expiration) claim to current time + 24 hours
                .expiration(new Date(System.currentTimeMillis() + expirationTimeMs))
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * Extracts the username (subject) from a given JWT token.
     * @param token The JWT string.
     * @return The username (subject) of the token.
     */
    public String getUsernameFromToken(String token) {
        // Parse the token, verify the signature, and get the payload (Claims)
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // FIX: Instead of returning the claims object's string representation,
        // return the specific 'sub' claim using getSubject().
        return claims.getSubject();
    }
}
