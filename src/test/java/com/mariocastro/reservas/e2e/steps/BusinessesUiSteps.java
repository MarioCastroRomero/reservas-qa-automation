package com.mariocastro.reservas.e2e.steps;

import com.mariocastro.reservas.e2e.PlaywrightHooks;
import com.mariocastro.reservas.e2e.pages.BusinessesPage;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import static com.mariocastro.reservas.e2e.support.TestUsers.createBusiness;
import static com.mariocastro.reservas.e2e.support.TestUsers.register;

public class BusinessesUiSteps {

    private BusinessesPage businessesPage(){
        return new BusinessesPage(PlaywrightHooks.page());
    }

    @Dado("que existe un negocio llamado {string}")
    public void businessExists(String name) {
        createBusiness(register("Pepe"), name);
    }

    @Cuando("la persona abre el catálogo de negocios")
    public void opensCatalog() {
        businessesPage().open();
    }

    @Cuando("la persona busca {string} en el catálogo")
    public void searchesCatalog(String text) {
        businessesPage().buscarEnCatalogo(text);
    }

    @Cuando("busca {string} en el catálogo")
    public void alsoSearchesCatalog(String text) {
        businessesPage().buscarEnCatalogo(text);
    }

    @Entonces("el catálogo muestra el negocio {string}")
    public void catalogShowsBusiness(String name) {
        businessesPage().buscaElNombreEnCatalogo(name);
    }

    @Entonces("el catálogo no muestra el negocio {string}")
    public void catalogHidesBusiness(String name) {
        businessesPage().elNombreNoEstaEnCatalogo(name);
    }

    @Entonces("el catálogo muestra el estado vacío")
    public void catalogShowsEmptyState() {
        businessesPage().catalogoVacio();
    }

    @Entonces("la URL contiene el parámetro {string}")
    public void urlContainsParam(String param) {
        businessesPage().laUrlContiene(param);
    }
}
