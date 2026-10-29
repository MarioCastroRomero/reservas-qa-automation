package com.mariocastro.reservas.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class MyBusinessesPage {

    private final Page page;

    public void open() {
        page.navigate("/mis-negocios");
    }

    public MyBusinessesPage(Page page) {
        this.page = page;
    }

    public void publiarNegocio(String negocio, String cif, String address) {
        open();
        pulsarBoton("Crear negocio");
        page.getByPlaceholder("Ej.: Peluquería Rizos").fill(negocio);
        page.getByPlaceholder("B12345678").fill(cif);
        page.getByPlaceholder("Calle Mayor 10, Madrid").fill(address);
        pulsarBoton("Publicar negocio");
    }

    public Locator estaNegocio(String negocio) {
        return page.locator("article.owner-card").filter(new Locator.FilterOptions().setHasText(negocio));
    }

    public Locator errorFormularioNegocio(String tipo) {
        return page.locator("p.field-error").filter(new Locator.FilterOptions().setHasText(tipo));
    }

    public void crearNegocioExistente(String nombre) {
        publiarNegocio(nombre, "B22345678", "Calle Mayor 10");
    }

    public void eliminarNegocio(String nombre) {
        String nombreBoton = "Eliminar " + nombre;
        pulsarBoton(nombreBoton);
    }

    public void confirmarEliminar() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Eliminar").setExact(true)).click();
    }

    public Locator noEstaElNegocio(String negocio) {
        return page.locator("article.owner-card").filter(new Locator.FilterOptions().setHasText(negocio));
    }

    private void pulsarBoton(String boton) {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(boton)).click();
    }

}
