package com.mariocastro.reservas.dto;

import com.mariocastro.reservas.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private UUID uuid;
    private String email;
    private String name;
    private String lastName;
    private Role role;
}
