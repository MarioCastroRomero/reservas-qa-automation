package com.mariocastro.reservas.dto;

import com.mariocastro.reservas.model.Booking;
import com.mariocastro.reservas.model.BookingStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID uuid;

    @NotNull(message = "Business is required")
    private UUID businessId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID holderId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BookingStatus status;

    @NotNull(message = "Booking time is required")
    @Future(message = "Booking time must be in the future")
    private LocalDateTime bookingTime;

    @PositiveOrZero(message = "Price must not be negative")
    private BigDecimal price;

    @Size(max = 500, message = "Note must be at most 500 characters")
    private String note;

    @NotNull(message = "Party size is required")
    @Min(value = 1, message = "There must be at least 1 person")
    private Integer partySize;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String businessName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String businessAddress;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String holderName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String holderEmail;

    public static BookingDto fromBooking(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setUuid(booking.getUuid());
        bookingDto.setStatus(booking.getStatus());
        bookingDto.setBookingTime(booking.getBookingTime());
        bookingDto.setPrice(booking.getPrice());
        bookingDto.setNote(booking.getNote());
        bookingDto.setPartySize(booking.getPartySize());
        bookingDto.setCreatedAt(booking.getCreatedAt());
        bookingDto.setBusinessId(booking.getBusiness().getUuid());
        bookingDto.setHolderId(booking.getHolder().getUuid());
        bookingDto.setBusinessName(booking.getBusiness().getName());
        bookingDto.setBusinessAddress(booking.getBusiness().getAddress());
        bookingDto.setHolderName(BusinessDto.fullName(booking.getHolder().getName(), booking.getHolder().getLastName()));
        bookingDto.setHolderEmail(booking.getHolder().getEmail());
        return bookingDto;
    }

    public Booking toBooking() {
        Booking booking = new Booking();
        booking.setBookingTime(this.getBookingTime());
        booking.setPrice(this.getPrice());
        booking.setNote(this.getNote());
        booking.setPartySize(this.getPartySize());
        return booking;
    }
}
