package com.mariocastro.reservas.model;

import java.util.EnumSet;
import java.util.Set;

public enum BookingStatus {
    PENDING,
    CONFIRMED,
    CANCELLED,
    COMPLETED,
    REJECTED,
    NO_SHOW;

    public static final Set<BookingStatus> ACTIVE = EnumSet.of(PENDING, CONFIRMED);

    public boolean canTransitionTo(BookingStatus next) {
        return switch (this) {
            case PENDING -> next == CONFIRMED || next == CANCELLED || next == REJECTED;
            case CONFIRMED -> next == COMPLETED || next == CANCELLED || next == NO_SHOW;
            case CANCELLED, COMPLETED, REJECTED, NO_SHOW -> false;
        };
    }
}
