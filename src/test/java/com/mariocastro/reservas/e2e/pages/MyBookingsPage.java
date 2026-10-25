package com.mariocastro.reservas.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.SelectOption;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;


public class MyBookingsPage {

    private final Page page;

    public void open() {
        page.navigate("/reservas");
    }

    public MyBookingsPage(Page page) {
        this.page = page;
    }

    public void hacerReserva(String negocio) {
        buscarNegocioParaReservar(negocio);
        page.getByText("Solicitar reserva").click();
    }

    public void hacerReservaPersonasDias(String negocio, int personas, int dias) {
        buscarNegocioParaReservar(negocio);
        cambiarFechaEnReserva(personas, dias);
        page.getByText("Solicitar reserva").click();
    }


    public void reservaCreadaCorrectamente(String negocio, String estado) {
        Locator tarjetaReserva = page.locator("article.booking-card")
                .filter(new Locator.FilterOptions().setHasText(negocio));
        assertThat(tarjetaReserva).isVisible();
        Locator badgeEstado = tarjetaReserva.locator(".badge, [class*='badge'], [class*='status']")
                .filter(new Locator.FilterOptions().setHasText(estado));
        assertThat(badgeEstado).isVisible();
        assertThat(badgeEstado).hasText(estado);
    }

    public void reservaFechaPasada(String negocio) {
        buscarNegocioParaReservar(negocio);
        cambiarFechaEnReserva(2, -3);
        page.getByText("Solicitar reserva").click();
    }

    public void errorEnPaginaReserva(String field) {
        assertThat(page.getByLabel(field)).hasAttribute("aria-invalid", "true");
    }

    public void cancelarReserva() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Cancelar")).click();
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sí, cancelar")).click();
    }

    public void filtrarReserva(String estado) {
        page.locator("nav a[href='/mis-reservas']").click();
        page.getByText(estado).click();
    }

    public void noApareceReserva(String negocio) {
        Locator reserva = page.locator("article.booking-card")
                .filter(new Locator.FilterOptions().setHasText(negocio));

        assertThat(reserva).hasCount(0);
    }


    private void buscarNegocioParaReservar(String negocio) {
        Locator byLabel = page.getByPlaceholder("Buscar por nombre o dirección...");
        byLabel.fill(negocio);
        byLabel.press("Enter");
        page.locator("a.business-card")
                .filter(new Locator.FilterOptions().setHasText(negocio))
                .click();
    }

    private void cambiarFechaEnReserva(int personas, int dias) {
        LocalDateTime targetDate = LocalDateTime.now().plusDays(dias).withHour(12).withMinute(0).withSecond(0);
        String formattDate = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        page.getByLabel("Fecha y hora").fill(formattDate);
        page.getByLabel("personas").fill(String.valueOf(personas));
    }


}
