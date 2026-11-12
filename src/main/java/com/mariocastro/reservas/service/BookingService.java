package com.mariocastro.reservas.service;

import com.mariocastro.reservas.dto.BookingDto;
import com.mariocastro.reservas.exception.ValidateException;
import com.mariocastro.reservas.model.CancelledBy;
import com.mariocastro.reservas.model.OpeningHours;
import com.mariocastro.reservas.repository.OpeningHoursRepository;
import com.mariocastro.reservas.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Value;
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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    private final OpeningHoursRepository openingHoursRepository;

    private final ReviewRepository reviewRepository;

    @Value("${app.booking.cancellation-min-hours:24}")
    private long cancellationMinHours;

    private final UserService userService;

    private final BusinessService businessService;

    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public BookingDto getBooking(String uuidStr) {
        Booking booking = findBooking(Utils.getUuid(uuidStr));
        authorizationService.requireCanViewBooking(booking);
        return toDto(booking);
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
        return page.map(this::toDto).getContent();
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
        return page.map(this::toDto).getContent();
    }

    @Transactional
    public BookingDto updateBooking(String uuidStr, UpdateBookingRequest request) {
        Booking booking = findBooking(Utils.getUuid(uuidStr));
        authorizationService.requireHolder(booking);
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException("Only pending bookings can be modified");
        }
        validateSlot(booking.getBusiness(), request.getBookingTime(), request.getPartySize(), booking.getUuid());
        booking.setBookingTime(request.getBookingTime());
        booking.setPartySize(request.getPartySize());
        booking.setNote(request.getNote());
        return toDto(bookingRepository.save(booking));
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

        validateSlot(business, bookingDto.getBookingTime(), bookingDto.getPartySize(), null);

        Booking booking = bookingDto.toBooking();
        booking.setHolder(holder);
        booking.setBusiness(business);
        booking.setStatus(BookingStatus.PENDING);
        return toDto(bookingRepository.save(booking));
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
    public BookingDto cancel(String uuidStr, String reason) {
        Booking booking = findBooking(Utils.getUuid(uuidStr));
        authorizationService.requireCanViewBooking(booking);

        boolean byHolder = authorizationService.isHolder(booking);
        if (byHolder && booking.getStatus().canTransitionTo(BookingStatus.CANCELLED)
                && LocalDateTime.now().plusHours(cancellationMinHours).isAfter(booking.getBookingTime())) {
            throw new ConflictException(
                    "Bookings can only be cancelled at least " + cancellationMinHours + " hours in advance");
        }
        booking.setCancelledBy(byHolder ? CancelledBy.HOLDER : CancelledBy.BUSINESS);
        booking.setCancellationReason(blankToNull(reason));
        return transition(booking, BookingStatus.CANCELLED);
    }

    @Transactional
    public BookingDto reject(String uuidStr, String reason) {
        Booking booking = findBooking(Utils.getUuid(uuidStr));
        authorizationService.requireBookingManager(booking);
        booking.setCancelledBy(CancelledBy.BUSINESS);
        booking.setCancellationReason(blankToNull(reason));
        return transition(booking, BookingStatus.REJECTED);
    }

    @Transactional
    public BookingDto noShow(String uuidStr) {
        Booking booking = findBooking(Utils.getUuid(uuidStr));
        authorizationService.requireBookingManager(booking);
        if (booking.getStatus() == BookingStatus.CONFIRMED && booking.getBookingTime().isAfter(LocalDateTime.now())) {
            throw new ConflictException("Cannot mark as no-show before the booking time");
        }
        return transition(booking, BookingStatus.NO_SHOW);
    }

    private void validateSlot(Business business, LocalDateTime time, int partySize, UUID excludeUuid) {
        List<OpeningHours> schedule = openingHoursRepository.findByBusinessUuidOrderByDayOfWeek(business.getUuid());
        if (!schedule.isEmpty()) {
            LocalTime at = time.toLocalTime();
            boolean open = schedule.stream().anyMatch(h -> h.getDayOfWeek() == time.getDayOfWeek()
                    && !at.isBefore(h.getOpenTime()) && at.isBefore(h.getCloseTime()));
            if (!open) {
                throw new ValidateException("Business is closed at the requested time");
            }
        }

        Integer capacity = business.getMaxCapacityPerSlot();
        if (capacity != null) {
            LocalDateTime from = time.truncatedTo(ChronoUnit.HOURS);
            long taken = bookingRepository.sumPartySizeByBusinessAndSlot(business.getUuid(), from, from.plusHours(1),
                    BookingStatus.ACTIVE, excludeUuid == null ? new UUID(0, 0) : excludeUuid);
            long available = Math.max(0, capacity - taken);
            if (partySize > available) {
                throw new ConflictException("Not enough capacity in this time slot (available: " + available + ")");
            }
        }
    }

    private BookingDto toDto(Booking booking) {
        return BookingDto.fromBooking(booking, reviewRepository.existsByBookingUuid(booking.getUuid()));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BookingDto transition(Booking booking, BookingStatus next) {
        if (!booking.getStatus().canTransitionTo(next)) {
            throw new ConflictException("Cannot change a booking from " + booking.getStatus() + " to " + next);
        }
        booking.setStatus(next);
        return toDto(bookingRepository.save(booking));
    }

    private Booking findBooking(UUID uuid) {
        return bookingRepository.findById(uuid).orElseThrow(() -> new NotFoundException("Booking not found"));
    }
}

