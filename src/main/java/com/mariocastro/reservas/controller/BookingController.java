package com.mariocastro.reservas.controller;

import com.mariocastro.reservas.dto.BookingDto;
import com.mariocastro.reservas.dto.UpdateBookingRequest;
import com.mariocastro.reservas.model.BookingStatus;
import com.mariocastro.reservas.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    @GetMapping("/{uuid}")
    public BookingDto getBooking(@PathVariable String uuid) {
        return bookingService.getBooking(uuid);
    }

    @GetMapping("/mine")
    public List<BookingDto> getMyBookings(
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 20, sort = "bookingTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return bookingService.getMyBookings(status, pageable);
    }

    @PostMapping
    public BookingDto saveBooking(@Validated @RequestBody BookingDto bookingDto) {
        return bookingService.saveBooking(bookingDto);
    }

    @PutMapping("/{uuid}")
    public BookingDto updateBooking(@PathVariable String uuid, @Valid @RequestBody UpdateBookingRequest request) {
        return bookingService.updateBooking(uuid, request);
    }

    @PatchMapping("/{uuid}/confirm")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public BookingDto confirm(@PathVariable String uuid) {
        return bookingService.confirm(uuid);
    }

    @PatchMapping("/{uuid}/complete")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public BookingDto complete(@PathVariable String uuid) {
        return bookingService.complete(uuid);
    }

    @PatchMapping("/{uuid}/cancel")
    public BookingDto cancel(@PathVariable String uuid) {
        return bookingService.cancel(uuid);
    }
}
