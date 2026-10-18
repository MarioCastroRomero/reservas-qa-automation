package com.mariocastro.reservas.repository;

import com.mariocastro.reservas.model.Booking;
import com.mariocastro.reservas.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Page<Booking> findByHolderUuid(UUID holderUuid, Pageable pageable);

    Page<Booking> findByHolderUuidAndStatus(UUID holderUuid, BookingStatus status, Pageable pageable);

    Page<Booking> findByBusinessUuid(UUID businessUuid, Pageable pageable);

    Page<Booking> findByBusinessUuidAndStatus(UUID businessUuid, BookingStatus status, Pageable pageable);

    boolean existsByBusinessUuidAndStatusIn(UUID businessUuid, Collection<BookingStatus> statuses);
}
