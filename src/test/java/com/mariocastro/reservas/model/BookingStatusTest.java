package com.mariocastro.reservas.model;

import org.junit.jupiter.api.Test;

import static com.mariocastro.reservas.model.BookingStatus.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingStatusTest {

    @Test
    void pendingCanBeConfirmedOrCancelled() {
        assertTrue(PENDING.canTransitionTo(CONFIRMED));
        assertTrue(PENDING.canTransitionTo(CANCELLED));
        assertFalse(PENDING.canTransitionTo(COMPLETED));
    }

    @Test
    void confirmedCanBeCompletedOrCancelled() {
        assertTrue(CONFIRMED.canTransitionTo(COMPLETED));
        assertTrue(CONFIRMED.canTransitionTo(CANCELLED));
        assertFalse(CONFIRMED.canTransitionTo(PENDING));
    }

    @Test
    void pendingCanBeRejectedButNotMarkedNoShow() {
        assertTrue(PENDING.canTransitionTo(REJECTED));
        assertFalse(PENDING.canTransitionTo(NO_SHOW));
    }

    @Test
    void confirmedCanBeMarkedNoShowButNotRejected() {
        assertTrue(CONFIRMED.canTransitionTo(NO_SHOW));
        assertFalse(CONFIRMED.canTransitionTo(REJECTED));
    }

    @Test
    void terminalStatusesHaveNoTransitions() {
        for (BookingStatus next : values()) {
            assertFalse(CANCELLED.canTransitionTo(next));
            assertFalse(COMPLETED.canTransitionTo(next));
            assertFalse(REJECTED.canTransitionTo(next));
            assertFalse(NO_SHOW.canTransitionTo(next));
        }
    }
}
