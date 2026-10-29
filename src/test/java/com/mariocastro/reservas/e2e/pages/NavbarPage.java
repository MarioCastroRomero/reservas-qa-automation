package com.mariocastro.reservas.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class NavbarPage {
    private final Page page;

    private final Locator linkReservas;

    public NavbarPage(Page page) {
        this.page = page;
        this.linkReservas = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Reservas").setExact(true));
    }

    public void clickReservas() {
        linkReservas.click();
    }
}