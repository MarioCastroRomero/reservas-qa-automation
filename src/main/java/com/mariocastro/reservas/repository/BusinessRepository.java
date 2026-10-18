package com.mariocastro.reservas.repository;

import com.mariocastro.reservas.model.Business;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {
    Optional<Business> findByCif(String cif);

    Page<Business> findByOwnerUuidAndActiveTrue(UUID ownerUuid, Pageable pageable);

    Page<Business> findByActiveTrue(Pageable pageable);

    Page<Business> findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndAddressContainingIgnoreCase(
            String name, String address, Pageable pageable);

}
