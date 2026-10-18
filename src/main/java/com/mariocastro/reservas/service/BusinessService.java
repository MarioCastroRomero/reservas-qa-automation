package com.mariocastro.reservas.service;

import com.mariocastro.reservas.dto.BusinessDto;
import com.mariocastro.reservas.exception.ConflictException;
import com.mariocastro.reservas.exception.NotFoundException;
import com.mariocastro.reservas.model.Business;
import com.mariocastro.reservas.model.BookingStatus;
import com.mariocastro.reservas.model.Role;
import com.mariocastro.reservas.model.User;
import com.mariocastro.reservas.repository.BookingRepository;
import com.mariocastro.reservas.repository.BusinessRepository;
import com.mariocastro.reservas.security.AuthorizationService;
import com.mariocastro.reservas.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;

    private final BookingRepository bookingRepository;

    private final UserService userService;

    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public BusinessDto getBusiness(String uuidStr) {
        return BusinessDto.fromBusiness(findBusiness(Utils.getUuid(uuidStr)));
    }

    @Transactional(readOnly = true)
    public List<BusinessDto> getMyBusinesses(Pageable pageable) {
        UUID ownerUuid = authorizationService.currentUser().getUuid();
        return businessRepository.findByOwnerUuidAndActiveTrue(ownerUuid, pageable)
                .map(BusinessDto::fromBusiness)
                .getContent();
    }

    @Transactional(readOnly = true)
    public List<BusinessDto> searchBusinesses(String query, Pageable pageable) {
        String text = query == null ? "" : query.trim();
        var page = text.isEmpty()
                ? businessRepository.findByActiveTrue(pageable)
                : businessRepository.findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndAddressContainingIgnoreCase(
                        text, text, pageable);
        return page.map(BusinessDto::fromBusiness).getContent();
    }

    @Transactional
    public BusinessDto saveBusiness(BusinessDto businessDto) {
        assertCifAvailable(businessDto.getCif(), null);

        User owner = userService.findUserOptional(authorizationService.currentUser().getUuid())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (owner.getRole() == Role.USER) {
            owner.setRole(Role.BUSINESS_OWNER);
        }

        Business business = businessDto.toBusiness();
        business.setOwner(owner);
        return BusinessDto.fromBusiness(businessRepository.save(business));
    }

    @Transactional
    public BusinessDto updateBusiness(String uuidStr, BusinessDto businessDto) {
        Business business = findBusiness(Utils.getUuid(uuidStr));
        authorizationService.requireBusinessOwner(business);

        assertCifAvailable(businessDto.getCif(), business.getUuid());
        business.setName(businessDto.getName());
        business.setCif(businessDto.getCif());
        business.setAddress(businessDto.getAddress());
        return BusinessDto.fromBusiness(businessRepository.save(business));
    }

    @Transactional
    public void deleteBusiness(String uuidStr) {
        Business business = findBusiness(Utils.getUuid(uuidStr));
        authorizationService.requireBusinessOwner(business);

        if (bookingRepository.existsByBusinessUuidAndStatusIn(business.getUuid(), BookingStatus.ACTIVE)) {
            throw new ConflictException("Business has pending or confirmed bookings");
        }
        business.setActive(false);
        businessRepository.save(business);
    }

    private void assertCifAvailable(String cif, UUID currentBusinessUuid) {
        Optional<Business> existing = businessRepository.findByCif(cif.trim().toUpperCase());
        if (existing.isPresent() && !existing.get().getUuid().equals(currentBusinessUuid)) {
            throw new ConflictException("CIF already exists");
        }
    }

    public Business findBusiness(UUID uuid) {
        return findBusinessOptional(uuid)
                .filter(Business::isActive)
                .orElseThrow(() -> new NotFoundException("Business not found"));
    }

    public Optional<Business> findBusinessOptional(UUID uuid) {
        return businessRepository.findById(uuid);
    }
}
