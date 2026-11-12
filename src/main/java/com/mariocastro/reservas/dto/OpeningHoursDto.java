package com.mariocastro.reservas.dto;

import com.mariocastro.reservas.model.OpeningHours;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpeningHoursDto {

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Opening time is required")
    private LocalTime openTime;

    @NotNull(message = "Closing time is required")
    private LocalTime closeTime;

    public static OpeningHoursDto fromEntity(OpeningHours hours) {
        return new OpeningHoursDto(hours.getDayOfWeek(), hours.getOpenTime(), hours.getCloseTime());
    }
}
