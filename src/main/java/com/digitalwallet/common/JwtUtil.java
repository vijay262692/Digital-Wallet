package com.digitalwallet.common;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Change this secret in production — minimum 32 chars
    private static final String SECRET = "digital-wallet-super-secret-key-32chars!";
    private static final long ACCESS_EXPIRY_MS  = 15 * 60 * 1000;      // 15 min
    private static final long REFRESH_EXPIRY_MS = 7 * 24 * 60 * 60 * 1000L; // 7 days

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateAccessToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("type", "ACCESS")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRY_MS))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .claim("type", "REFRESH")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRY_MS))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isValid(String token) {
        try { parseToken(token); return true; }
        catch (JwtException | IllegalArgumentException e) { return false; }
    }

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }
}