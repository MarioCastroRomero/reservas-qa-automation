package com.mariocastro.reservas.service;

import com.mariocastro.reservas.dto.AuthResponse;
import com.mariocastro.reservas.dto.LoginRequest;
import com.mariocastro.reservas.dto.UserDto;
import com.mariocastro.reservas.security.JwtService;
import com.mariocastro.reservas.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtService.generateToken(principal);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .uuid(principal.getUuid())
                .email(principal.getEmail())
                .name(principal.getName())
                .lastName(principal.getLastName())
                .role(principal.getRole())
                .build();
    }

    public AuthResponse register(UserDto userDto) {
        UserDto savedUser = userService.saveUser(userDto);
        String token = generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .uuid(savedUser.getUuid())
                .email(savedUser.getEmail())
                .name(savedUser.getName())
                .lastName(savedUser.getLastName())
                .role(savedUser.getRole())
                .build();
    }

    public String generateToken(UserDto savedUser) {
        UserPrincipal principal = (UserPrincipal) userDetailsService.loadUserByUsername(savedUser.getEmail());
        return jwtService.generateToken(principal);
    }
}
