package com.example.authservice;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    @Test
    void testGenerateToken() {
        String username = "testuser";

        String token = JwtUtil.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        // JWT should have 3 parts separated by dots
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length);
    }
}

