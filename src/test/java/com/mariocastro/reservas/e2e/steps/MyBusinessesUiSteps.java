package com.mariocastro.reservas.e2e.steps;

import com.mariocastro.reservas.e2e.PlaywrightHooks;
import com.mariocastro.reservas.e2e.pages.MyBusinessesPage;
import com.mariocastro.reservas.e2e.pages.NavbarPage;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import com.microsoft.playwright.Locator;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class MyBusinessesUiSteps {

    private MyBusinessesPage businessesPage() {
        return new MyBusinessesPage(PlaywrightHooks.page());
    }

    public NavbarPage navbarPage() {
        return new NavbarPage(PlaywrightHooks.page());
    }


    @Dado("que la persona ya tiene publicado el negocio {string}")
    public void personHasBusiness(String name) {
        businessesPage().crearNegocioExistente(name);
    }

    @Cuando("la persona publica el negocio {string} con CIF {string} y dirección {string}")
    public void publishesBusiness(String name, String cif, String address) {
        businessesPage().publiarNegocio(name,cif,address);
    }

    @Cuando("la persona elimina el negocio {string} y confirma en el modal")
    public void deletesBusiness(String name) {
        businessesPage().eliminarNegocio(name);
        businessesPage().confirmarEliminar();
    }

    @Entonces("\"Mis negocios\" muestra el negocio {string}")
    public void myBusinessesShows(String name) {
        Locator tarjeta = businessesPage().estaNegocio(name);
        assertThat(tarjeta).isVisible();
    }

    @Entonces("\"Mis negocios\" no muestra el negocio {string}")
    public void myBusinessesHides(String name) {
        Locator tarjeta = businessesPage().estaNegocio(name);
        assertThat(tarjeta).isHidden();
    }

    @Entonces("el formulario de negocio muestra un error en {string}")
    public void businessFormShowsError(String field) {
        Locator mensajeError = businessesPage().errorFormularioNegocio(field);
        assertThat(mensajeError).isVisible();
    }

    @Entonces("el menú principal muestra el negocio {string}")
    public void menuShowsLink(String name) {
        navbarPage().clickReservas();
    }
}
