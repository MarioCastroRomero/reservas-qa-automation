package com.mariocastro.reservas.service;

import com.mariocastro.reservas.dto.AuthResponse;
import com.mariocastro.reservas.dto.LoginRequest;
import com.mariocastro.reservas.dto.UserDto;
import com.mariocastro.reservas.exception.ValidateException;
import com.mariocastro.reservas.model.User;
import com.mariocastro.reservas.security.JwtService;
import com.mariocastro.reservas.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setUuid(UUID.randomUUID());
        sampleUser.setName("Juan");
        sampleUser.setLastName("Perez");
        sampleUser.setEmail("juan@example.com");
        sampleUser.setPassword("encodedPassword");
        sampleUser.setBirthDate(LocalDate.of(1990, 1, 1));

        userPrincipal = new UserPrincipal(sampleUser);
    }

    @Test
    void testLoginSuccess() {
        LoginRequest loginRequest = new LoginRequest("juan@example.com", "Password123!");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(jwtService.generateToken(userPrincipal)).thenReturn("mocked-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mocked-jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(sampleUser.getEmail(), response.getEmail());
        assertEquals(sampleUser.getName(), response.getName());
        assertEquals(sampleUser.getUuid(), response.getUuid());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(userPrincipal);
    }

    @Test
    void testLoginInvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest("juan@example.com", "WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void testRegisterSuccess() throws ValidateException {
        UserDto userDto = UserDto.fromUser(sampleUser);
        userDto.setPassword("Password123!");

        when(userService.saveUser(userDto)).thenReturn(userDto);
        when(userDetailsService.loadUserByUsername(userDto.getEmail())).thenReturn(userPrincipal);
        when(jwtService.generateToken(userPrincipal)).thenReturn("registered-jwt-token");

        AuthResponse response = authService.register(userDto);

        assertNotNull(response);
        assertEquals("registered-jwt-token", response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(userDto.getEmail(), response.getEmail());

        verify(userService).saveUser(userDto);
        verify(jwtService).generateToken(userPrincipal);
    }
}
