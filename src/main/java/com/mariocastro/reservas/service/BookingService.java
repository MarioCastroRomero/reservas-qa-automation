package com.mariocastro.reservas.service;

import com.mariocastro.reservas.dto.BookingDto;
import com.mariocastro.reservas.dto.UpdateBookingRequest;
import com.mariocastro.reservas.exception.ConflictException;
import com.mariocastro.reservas.exception.ForbiddenException;
import com.mariocastro.reservas.exception.NotFoundException;
import com.mariocastro.reservas.model.Booking;
import com.mariocastro.reservas.model.BookingStatus;
import com.mariocastro.reservas.model.Business;
import com.mariocastro.reservas.model.User;
import com.mariocastro.reservas.repository.BookingRepository;
import com.mariocastro.reservas.security.AuthorizationService;
import com.mariocastro.reservas.security.UserPrincipal;
import com.mariocastro.reservas.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    private final UserService userService;

    private final BusinessService businessService;

    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public BookingDto getBooking(String uuidStr) {
        Booking booking = findBooking(Utils.getUuid(uuidStr));
        authorizationService.requireCanViewBooking(booking);
        return BookingDto.fromBooking(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getMyBookings(Pageable pageable) {
        return getMyBookings(null, pageable);
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getMyBookings(BookingStatus status, Pageable pageable) {
        UUID holderUuid = authorizationService.currentUser().getUuid();
        var page = status == null
                ? bookingRepository.findByHolderUuid(holderUuid, pageable)
                : bookingRepository.findByHolderUuidAndStatus(holderUuid, status, pageable);
        return page.map(BookingDto::fromBooking).getContent();
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getBusinessBookings(String businessUuidStr, Pageable pageable) {
        return getBusinessBookings(businessUuidStr, null, pageable);
    }

    @Transactional(readOnly = true)
    public List<BookingDto> getBusinessBookings(String businessUuidStr, BookingStatus status, Pageable pageable) {
        Business business = businessService.findBusiness(Utils.getUuid(businessUuidStr));
        authorizationService.requireBusinessOwner(business);
        var page = status == null
                ? bookingRepository.findByBusinessUuid(business.getUuid(), pageable)
                : bookingRepository.findByBusinessUuidAndStatus(business.getUuid(), status, pageable);
        return page.map(BookingDto::fromBooking).getContent();
    }

    @Transactional
    public BookingDto updateBooking(String uuidStr, UpdateBookingRequest request) {
        Booking booking = findBooking(Utils.getUuid(uuidStr));
        authorizationService.requireHolder(booking);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException("Only pending bookings can be modified");
        }
        booking.setBookingTime(request.getBookingTime());
        booking.setPartySize(request.getPartySize());
        booking.setNote(request.getNote());
        return BookingDto.fromBooking(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto saveBooking(BookingDto bookingDto) {
        UserPrincipal current = authorizationService.currentUser();
        Business business = businessService.findBusiness(bookingDto.getBusinessId());
        if (business.isOwnedBy(current.getUuid())) {
            throw new ForbiddenException("Owners cannot book their own business");
        }
        User holder = userService.findUserOptional(current.getUuid())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Booking booking = bookingDto.toBooking();
        booking.setHolder(holder);
        booking.setBusiness(business);
        booking.setStatus(BookingStatus.PENDING);
        return BookingDto.fromBooking(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto confirm(String uuidStr) {
        Booking booking = findBooking(Utils.getUuid(uuidStr));
        authorizationService.requireBookingManager(booking);
        return transition(booking, BookingStatus.CONFIRMED);
    }

    @Transactional
    public BookingDto complete(String uuidStr) {
        Booking booking = findBooking(Utils.getUuid(uuidStr));
        authorizationService.requireBookingManager(booking);
        return transition(booking, BookingStatus.COMPLETED);
    }

    @Transactional
    public BookingDto cancel(String uuidStr) {
        Booking booking = findBooking(Utils.getUuid(uuidStr));
        authorizationService.requireCanViewBooking(booking);
        return transition(booking, BookingStatus.CANCELLED);
    }

    private BookingDto transition(Booking booking, BookingStatus next) {
        if (!booking.getStatus().canTransitionTo(next)) {
            throw new ConflictException("Cannot change a booking from " + booking.getStatus() + " to " + next);
        }
        booking.setStatus(next);
        return BookingDto.fromBooking(bookingRepository.save(booking));
    }

    private Booking findBooking(UUID uuid) {
        return bookingRepository.findById(uuid).orElseThrow(() -> new NotFoundException("Booking not found"));
    }
}
