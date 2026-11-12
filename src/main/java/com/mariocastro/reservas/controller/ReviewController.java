package com.mariocastro.reservas.controller;

import com.mariocastro.reservas.dto.ReviewDto;
import com.mariocastro.reservas.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/booking/{uuid}/review")
    public ReviewDto createReview(@PathVariable String uuid, @Validated @RequestBody ReviewDto request) {
        return reviewService.createReview(uuid, request);
    }

    @GetMapping("/business/{uuid}/reviews")
    public List<ReviewDto> getReviews(
            @PathVariable String uuid,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return reviewService.getBusinessReviews(uuid, pageable);
    }
}
