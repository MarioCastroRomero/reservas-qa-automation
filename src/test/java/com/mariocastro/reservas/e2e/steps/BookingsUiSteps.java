package com.mariocastro.reservas.e2e.steps;

import com.mariocastro.reservas.e2e.PlaywrightHooks;
import com.mariocastro.reservas.e2e.pages.BusinessesPage;
import com.mariocastro.reservas.e2e.pages.MyBookingsPage;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.cucumber.java.PendingException;
import org.xml.sax.Locator;

import static com.mariocastro.reservas.e2e.support.TestUsers.createBusiness;
import static com.mariocastro.reservas.e2e.support.TestUsers.register;

public class BookingsUiSteps {

    private MyBookingsPage bookingPage(){
        return new MyBookingsPage(PlaywrightHooks.page());
    }
    @Dado("que otra persona es dueña del negocio {string}")
    public void anotherPersonOwnsBusiness(String name) {
        createBusiness(register("Pepe"), name);
    }

    @Dado("que la persona tiene una reserva pendiente en {string}")
    public void personHasPendingBooking(String business) {
        bookingPage().hacerReserva(business);
    }

    @Cuando("la persona reserva en {string} para {int} personas dentro de {int} días")
    public void booksBusiness(String business, int people, int days) {
        bookingPage().hacerReservaPersonasDias(business, people, days);
    }

    @Cuando("la persona intenta reservar en {string} con una fecha pasada")
    public void booksWithPastDate(String business) {
        bookingPage().reservaFechaPasada(business);
    }

    @Cuando("la persona cancela esa reserva desde \"Mis reservas\"")
    public void cancelsBooking() {
        bookingPage().cancelarReserva();
    }

    @Cuando("la persona filtra \"Mis reservas\" por el estado {string}")
    public void filtersBookingsByStatus(String status) {
        bookingPage().filtrarReserva(status);
    }

    @Entonces("\"Mis reservas\" muestra una reserva de {string} en estado {string}")
    public void myBookingsShows(String business, String status) {
        bookingPage().reservaCreadaCorrectamente(business,status);
    }

    @Entonces("el formulario de reserva muestra un error en {string}")
    public void bookingFormShowsError(String field) {
        bookingPage().errorEnPaginaReserva(field);
    }

    @Entonces("\"Mis reservas\" no muestra ninguna reserva de {string}")
    public void myBookingsHides(String business) {
        bookingPage().noApareceReserva(business);
    }
}
