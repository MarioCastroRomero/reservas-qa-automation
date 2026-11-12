package com.mariocastro.reservas.service;

import com.mariocastro.reservas.dto.BusinessDto;
import com.mariocastro.reservas.dto.OpeningHoursDto;
import com.mariocastro.reservas.exception.ValidateException;
import com.mariocastro.reservas.model.OpeningHours;
import com.mariocastro.reservas.exception.ConflictException;
import com.mariocastro.reservas.exception.NotFoundException;
import com.mariocastro.reservas.model.Business;
import com.mariocastro.reservas.model.BookingStatus;
import com.mariocastro.reservas.model.Role;
import com.mariocastro.reservas.model.User;
import com.mariocastro.reservas.repository.BookingRepository;
import com.mariocastro.reservas.repository.BusinessRepository;
import com.mariocastro.reservas.repository.OpeningHoursRepository;
import com.mariocastro.reservas.repository.ReviewRepository;
import com.mariocastro.reservas.security.AuthorizationService;
import com.mariocastro.reservas.util.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessService {

    private final BusinessRepository businessRepository;

    private final BookingRepository bookingRepository;

    private final ReviewRepository reviewRepository;

    private final OpeningHoursRepository openingHoursRepository;

    private final UserService userService;

    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public BusinessDto getBusiness(String uuidStr) {
        return toDto(findBusiness(Utils.getUuid(uuidStr)));
    }

    @Transactional(readOnly = true)
    public List<BusinessDto> getMyBusinesses(Pageable pageable) {
        UUID ownerUuid = authorizationService.currentUser().getUuid();
        return businessRepository.findByOwnerUuidAndActiveTrue(ownerUuid, pageable)
                .map(this::toDto)
                .getContent();
    }

    @Transactional(readOnly = true)
    public List<BusinessDto> searchBusinesses(String query, Pageable pageable) {
        String text = query == null ? "" : query.trim();
        var page = text.isEmpty()
                ? businessRepository.findByActiveTrue(pageable)
                : businessRepository.findByActiveTrueAndNameContainingIgnoreCaseOrActiveTrueAndAddressContainingIgnoreCase(
                        text, text, pageable);
        return page.map(this::toDto).getContent();
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
        return toDto(businessRepository.save(business));
    }

    @Transactional
    public BusinessDto updateBusiness(String uuidStr, BusinessDto businessDto) {
        Business business = findBusiness(Utils.getUuid(uuidStr));
        authorizationService.requireBusinessOwner(business);

        assertCifAvailable(businessDto.getCif(), business.getUuid());
        business.setName(businessDto.getName());
        business.setCif(businessDto.getCif());
        business.setAddress(businessDto.getAddress());
        business.setMaxCapacityPerSlot(businessDto.getMaxCapacityPerSlot());
        return toDto(businessRepository.save(business));
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

    @Transactional(readOnly = true)
    public List<OpeningHoursDto> getOpeningHours(String uuidStr) {
        Business business = findBusiness(Utils.getUuid(uuidStr));
        return openingHoursRepository.findByBusinessUuidOrderByDayOfWeek(business.getUuid()).stream()
                .map(OpeningHoursDto::fromEntity)
                .toList();
    }

    @Transactional
    public List<OpeningHoursDto> replaceOpeningHours(String uuidStr, List<OpeningHoursDto> slots) {
        Business business = findBusiness(Utils.getUuid(uuidStr));
        authorizationService.requireBusinessOwner(business);

        var seen = EnumSet.noneOf(DayOfWeek.class);
        for (OpeningHoursDto slot : slots) {
            if (slot == null || slot.getDayOfWeek() == null || slot.getOpenTime() == null || slot.getCloseTime() == null) {
                throw new ValidateException("Day of week, opening time and closing time are required");
            }
            if (!slot.getOpenTime().isBefore(slot.getCloseTime())) {
                throw new ValidateException("Opening time must be before closing time");
            }
            if (!seen.add(slot.getDayOfWeek())) {
                throw new ValidateException("Duplicate day: " + slot.getDayOfWeek());
            }
        }

        openingHoursRepository.deleteByBusinessUuid(business.getUuid());
        openingHoursRepository.flush();
        List<OpeningHours> saved = openingHoursRepository.saveAll(slots.stream().map(slot -> {
            OpeningHours hours = new OpeningHours();
            hours.setBusiness(business);
            hours.setDayOfWeek(slot.getDayOfWeek());
            hours.setOpenTime(slot.getOpenTime());
            hours.setCloseTime(slot.getCloseTime());
            return hours;
        }).toList());
        return saved.stream()
                .sorted(java.util.Comparator.comparing(OpeningHours::getDayOfWeek))
                .map(OpeningHoursDto::fromEntity)
                .toList();
    }

    private BusinessDto toDto(Business business) {
        BusinessDto dto = BusinessDto.fromBusiness(business);
        long count = reviewRepository.countByBusinessUuid(business.getUuid());
        dto.setReviewCount(count);
        if (count > 0) {
            Double avg = reviewRepository.avgRatingByBusiness(business.getUuid());
            dto.setAverageRating(avg == null ? null
                    : BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).doubleValue());
        }
        return dto;
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

