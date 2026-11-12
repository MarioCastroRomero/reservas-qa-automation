package com.mariocastro.reservas.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelBookingRequest {

    @Size(max = 500, message = "Reason must be at most 500 characters")
    private String reason;
}
