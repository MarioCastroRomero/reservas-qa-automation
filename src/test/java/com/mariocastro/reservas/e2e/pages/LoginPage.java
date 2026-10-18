package com.mariocastro.reservas.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class LoginPage {

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public void open() {
        page.navigate("/login");
    }

    public void logIn(String email, String password) {
        page.getByLabel("Email").fill(email);
        page.getByLabel("Contraseña").fill(password);
        submit();
    }

    public void submit() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Iniciar sesión")).click();
    }

    public Locator formError() {
        return page.getByRole(AriaRole.ALERT);
    }

    public Locator fieldErrors() {
        return page.locator(".field-error");
    }
}
