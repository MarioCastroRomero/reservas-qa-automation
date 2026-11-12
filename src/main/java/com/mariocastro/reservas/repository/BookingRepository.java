package com.mariocastro.reservas.repository;

import com.mariocastro.reservas.model.Booking;
import com.mariocastro.reservas.model.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Page<Booking> findByHolderUuid(UUID holderUuid, Pageable pageable);

    Page<Booking> findByHolderUuidAndStatus(UUID holderUuid, BookingStatus status, Pageable pageable);

    Page<Booking> findByBusinessUuid(UUID businessUuid, Pageable pageable);

    Page<Booking> findByBusinessUuidAndStatus(UUID businessUuid, BookingStatus status, Pageable pageable);

    boolean existsByBusinessUuidAndStatusIn(UUID businessUuid, Collection<BookingStatus> statuses);

    @Query("""
            select coalesce(sum(b.partySize), 0) from Booking b
            where b.business.uuid = :businessUuid
              and b.bookingTime >= :from and b.bookingTime < :to
              and b.status in :statuses
              and b.uuid <> :excludeUuid
            """)
    long sumPartySizeByBusinessAndSlot(@Param("businessUuid") UUID businessUuid,
                                       @Param("from") LocalDateTime from,
                                       @Param("to") LocalDateTime to,
                                       @Param("statuses") Collection<BookingStatus> statuses,
                                       @Param("excludeUuid") UUID excludeUuid);
}
