package com.mariocastro.reservas.security;

import com.mariocastro.reservas.exception.ForbiddenException;
import com.mariocastro.reservas.model.Booking;
import com.mariocastro.reservas.model.Business;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AuthorizationService {

    public UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new ForbiddenException("Authentication required");
        }
        return principal;
    }

    public void requireSelfOrAdmin(UUID userUuid) {
        UserPrincipal current = currentUser();
        if (!current.isAdmin() && !current.getUuid().equals(userUuid)) {
            throw new ForbiddenException("You can only access your own user");
        }
    }

    public void requireBusinessOwner(Business business) {
        UserPrincipal current = currentUser();
        if (!current.isAdmin() && !business.isOwnedBy(current.getUuid())) {
            throw new ForbiddenException("You are not the owner of this business");
        }
    }

    public boolean isHolder(Booking booking) {
        return booking.getHolder().getUuid().equals(currentUser().getUuid());
    }

    public boolean isBusinessOwner(Booking booking) {
        return booking.getBusiness().isOwnedBy(currentUser().getUuid());
    }

    public void requireCanViewBooking(Booking booking) {
        if (!currentUser().isAdmin() && !isHolder(booking) && !isBusinessOwner(booking)) {
            throw new ForbiddenException("You cannot access this booking");
        }
    }

    public void requireHolder(Booking booking) {
        if (!isHolder(booking)) {
            throw new ForbiddenException("Only the holder can modify this booking");
        }
    }

    public void requireBookingManager(Booking booking) {
        if (!currentUser().isAdmin() && !isBusinessOwner(booking)) {
            throw new ForbiddenException("Only the business owner can manage this booking");
        }
    }
}
