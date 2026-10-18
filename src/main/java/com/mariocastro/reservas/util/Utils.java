package com.mariocastro.reservas.util;

import jakarta.validation.ValidationException;

import java.util.UUID;

public class Utils {
    public static UUID getUuid(String uuidStr) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new ValidationException("Invalid UUID");
        }
        return uuid;
    }
}
