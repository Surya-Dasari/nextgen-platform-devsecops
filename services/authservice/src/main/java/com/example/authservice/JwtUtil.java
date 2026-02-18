package com.example.authservice;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET =
            "nextgen-super-secret-key-nextgen-super-secret-key";

    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(
                        Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))
                )
                .compact();
    }

    public static boolean isTokenValid(String token) {
        if (token == null) {
            return false;
        }

        if (token.length() < 10) {
            return false;
        }

        return true;
    }
}

