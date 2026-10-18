package com.mariocastro.reservas.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(
                jwtService,
                "secretKey",
                "5+tXo1NKVZhPHNzGss5oDNeFdxh98bwm5GWpCSVxM2h1+E4wMKZx1v0HSCcsgb4pHhcFA7Zuaw4Qy0P4s1SxWw=="
        );
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);

        userDetails = new User("test@example.com", "password123", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        String username = jwtService.extractUsername(token);
        assertEquals("test@example.com", username);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testTokenInvalidForDifferentUser() {
        String token = jwtService.generateToken(userDetails);
        UserDetails differentUser = new User("other@example.com", "password123", Collections.emptyList());

        assertFalse(jwtService.isTokenValid(token, differentUser));
    }

    @Test
    void testTokenExpiration() {
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);
        String expiredToken = jwtService.generateToken(userDetails);

        assertThrows(Exception.class, () -> jwtService.isTokenValid(expiredToken, userDetails));
    }
}
