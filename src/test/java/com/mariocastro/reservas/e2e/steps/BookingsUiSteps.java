package com.mariocastro.reservas.e2e.steps;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.cucumber.java.PendingException;

public class BookingsUiSteps {

    @Dado("que otra persona es dueña del negocio {string}")
    public void anotherPersonOwnsBusiness(String name) {
        throw new PendingException();
    }

    @Dado("que la persona tiene una reserva pendiente en {string}")
    public void personHasPendingBooking(String business) {
        throw new PendingException();
    }

    @Cuando("la persona reserva en {string} para {int} personas dentro de {int} días")
    public void booksBusiness(String business, int people, int days) {
        throw new PendingException();
    }

    @Cuando("la persona intenta reservar en {string} con una fecha pasada")
    public void booksWithPastDate(String business) {
        throw new PendingException();
    }

    @Cuando("la persona cancela esa reserva desde \"Mis reservas\"")
    public void cancelsBooking() {
        throw new PendingException();
    }

    @Cuando("la persona filtra \"Mis reservas\" por el estado {string}")
    public void filtersBookingsByStatus(String status) {
        throw new PendingException();
    }

    @Entonces("\"Mis reservas\" muestra una reserva de {string} en estado {string}")
    public void myBookingsShows(String business, String status) {
        throw new PendingException();
    }

    @Entonces("el formulario de reserva muestra un error en {string}")
    public void bookingFormShowsError(String field) {
        throw new PendingException();
    }

    @Entonces("\"Mis reservas\" no muestra ninguna reserva de {string}")
    public void myBookingsHides(String business) {
        throw new PendingException();
    }
}
