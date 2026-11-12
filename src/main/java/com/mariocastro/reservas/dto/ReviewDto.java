package com.mariocastro.reservas.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mariocastro.reservas.model.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID uuid;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID bookingId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID businessId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String authorName;

    @NotNull(message = "Rating must be between 1 and 5")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    @Size(max = 500, message = "Comment must be at most 500 characters")
    private String comment;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    public static ReviewDto fromReview(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setUuid(review.getUuid());
        dto.setBookingId(review.getBooking().getUuid());
        dto.setBusinessId(review.getBusiness().getUuid());
        dto.setAuthorName(BusinessDto.fullName(review.getAuthor().getName(), review.getAuthor().getLastName()));
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
