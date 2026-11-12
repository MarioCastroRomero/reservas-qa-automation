package com.mariocastro.reservas.service;

import com.mariocastro.reservas.dto.ReviewDto;
import com.mariocastro.reservas.exception.ConflictException;
import com.mariocastro.reservas.exception.ForbiddenException;
import com.mariocastro.reservas.exception.NotFoundException;
import com.mariocastro.reservas.model.Booking;
import com.mariocastro.reservas.model.BookingStatus;
import com.mariocastro.reservas.model.Review;
import com.mariocastro.reservas.repository.BookingRepository;
import com.mariocastro.reservas.repository.ReviewRepository;
import com.mariocastro.reservas.security.AuthorizationService;
import com.mariocastro.reservas.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    private final BookingRepository bookingRepository;

    private final BusinessService businessService;

    private final AuthorizationService authorizationService;

    @Transactional
    public ReviewDto createReview(String bookingUuidStr, ReviewDto request) {
        Booking booking = bookingRepository.findById(Utils.getUuid(bookingUuidStr))
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        if (!authorizationService.isHolder(booking)) {
            throw new ForbiddenException("Only the holder can review this booking");
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ConflictException("Only completed bookings can be reviewed");
        }
        if (reviewRepository.existsByBookingUuid(booking.getUuid())) {
            throw new ConflictException("This booking has already been reviewed");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setBusiness(booking.getBusiness());
        review.setAuthor(booking.getHolder());
        review.setRating(request.getRating());
        String comment = request.getComment();
        review.setComment(comment == null || comment.isBlank() ? null : comment.trim());
        return ReviewDto.fromReview(reviewRepository.save(review));
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getBusinessReviews(String businessUuidStr, Pageable pageable) {
        var business = businessService.findBusiness(Utils.getUuid(businessUuidStr));
        return reviewRepository.findByBusinessUuid(business.getUuid(), pageable)
                .map(ReviewDto::fromReview)
                .getContent();
    }
}
