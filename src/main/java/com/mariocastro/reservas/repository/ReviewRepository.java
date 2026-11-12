package com.mariocastro.reservas.repository;

import com.mariocastro.reservas.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    boolean existsByBookingUuid(UUID bookingUuid);

    Page<Review> findByBusinessUuid(UUID businessUuid, Pageable pageable);

    long countByBusinessUuid(UUID businessUuid);

    @Query("select avg(r.rating) from Review r where r.business.uuid = :businessUuid")
    Double avgRatingByBusiness(@Param("businessUuid") UUID businessUuid);
}
