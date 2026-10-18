package com.mariocastro.reservas.controller;

import com.mariocastro.reservas.dto.BookingDto;
import com.mariocastro.reservas.dto.BusinessDto;
import com.mariocastro.reservas.model.BookingStatus;
import com.mariocastro.reservas.service.BookingService;
import com.mariocastro.reservas.service.BusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    private final BookingService bookingService;

    @GetMapping
    public List<BusinessDto> search(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 12, sort = "name") Pageable pageable) {
        return businessService.searchBusinesses(q, pageable);
    }

    @GetMapping("/{uuid}")
    public BusinessDto getBusiness(@PathVariable String uuid) {
        return businessService.getBusiness(uuid);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public List<BusinessDto> getMyBusinesses(@PageableDefault(size = 50, sort = "name") Pageable pageable) {
        return businessService.getMyBusinesses(pageable);
    }

    @PostMapping
    public BusinessDto save(@Validated @RequestBody BusinessDto businessDto) {
        return businessService.saveBusiness(businessDto);
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public BusinessDto update(@PathVariable String uuid, @Validated @RequestBody BusinessDto businessDto) {
        return businessService.updateBusiness(uuid, businessDto);
    }

    @DeleteMapping("/{uuid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public void delete(@PathVariable String uuid) {
        businessService.deleteBusiness(uuid);
    }

    @GetMapping("/{uuid}/bookings")
    @PreAuthorize("hasAnyRole('BUSINESS_OWNER', 'ADMIN')")
    public List<BookingDto> getBookings(
            @PathVariable String uuid,
            @RequestParam(required = false) BookingStatus status,
            @PageableDefault(size = 20, sort = "bookingTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return bookingService.getBusinessBookings(uuid, status, pageable);
    }
}
