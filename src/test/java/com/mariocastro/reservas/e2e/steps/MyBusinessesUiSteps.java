package com.mariocastro.reservas.e2e.steps;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.cucumber.java.PendingException;

public class MyBusinessesUiSteps {

    @Dado("que la persona ya tiene publicado el negocio {string}")
    public void personHasBusiness(String name) {
        throw new PendingException();
    }

    @Cuando("la persona publica el negocio {string} con CIF {string} y dirección {string}")
    public void publishesBusiness(String name, String cif, String address) {
        throw new PendingException();
    }

    @Cuando("la persona elimina el negocio {string} y confirma en el modal")
    public void deletesBusiness(String name) {
        throw new PendingException();
    }

    @Entonces("\"Mis negocios\" muestra el negocio {string}")
    public void myBusinessesShows(String name) {
        throw new PendingException();
    }

    @Entonces("\"Mis negocios\" no muestra el negocio {string}")
    public void myBusinessesHides(String name) {
        throw new PendingException();
    }

    @Entonces("el formulario de negocio muestra un error en {string}")
    public void businessFormShowsError(String field) {
        throw new PendingException();
    }

    @Entonces("el menú principal muestra el enlace {string}")
    public void menuShowsLink(String name) {
        throw new PendingException();
    }
}
