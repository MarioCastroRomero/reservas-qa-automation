package com.mariocastro.reservas.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class BusinessesPage {

    private final Page page;

    public void open() {
        page.navigate("/negocios");
    }

    public BusinessesPage(Page page) {
        this.page = page;
    }

    public void buscarEnCatalogo(String name){
        Locator byLabel = page.getByPlaceholder("Buscar por nombre o dirección...");
        byLabel.fill(name);
        byLabel.press("Enter");
    }

    public void buscaElNombreEnCatalogo(String name){
        assertThat(tarjeta(name)).isVisible();
    }

    public void elNombreNoEstaEnCatalogo(String name){
        assertThat(tarjeta(name)).hasCount(0);
    }

    public void catalogoVacio(){
        assertThat(page.locator(".empty")).isVisible();
        assertThat(page.locator(".business-card")).hasCount(0);
    }

    public void laUrlContiene(String text) {
        assertThat(page).hasURL(Pattern.compile(".*[?&]" + escapeRegex(text) + "(&.*)?$"));
    }

    private static String escapeRegex(String text) {
        return text.replaceAll("[^\\w]", "\\\\$0");
    }

    private Locator tarjeta(String name) {
        return page.locator(".business-card")
                .filter(new Locator.FilterOptions().setHasText(name));
    }
}
