package com.mariocastro.reservas.repository;

import com.mariocastro.reservas.model.OpeningHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OpeningHoursRepository extends JpaRepository<OpeningHours, UUID> {
    List<OpeningHours> findByBusinessUuidOrderByDayOfWeek(UUID businessUuid);

    void deleteByBusinessUuid(UUID businessUuid);
}
