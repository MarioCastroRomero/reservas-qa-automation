package com.mariocastro.reservas.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

public class RegisterPage {

    private final Page page;

    public RegisterPage(Page page) {
        this.page = page;
    }

    public void open() {
        page.navigate("/registro");
    }

    public void register(String name, String lastName , String email, String birthDate, String password, String confirmPassword) {
        page.getByLabel("Nombre").fill(name);
        page.getByLabel("Apellidos").fill(lastName);
        page.getByLabel("Fecha de nacimiento").fill(birthDate);
        page.getByLabel("Email").fill(email);
        page.getByLabel("Contraseña", new Page.GetByLabelOptions().setExact(true)).fill(password);
        page.getByLabel("Repite la contraseña", new Page.GetByLabelOptions().setExact(true)).fill(confirmPassword);
        submit();
    }

    public void submit() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Crear cuenta")).click();
    }

    public Locator fieldError(String field) {
        return page.locator(".field")
                .filter(new Locator.FilterOptions().setHas(
                        page.getByText(field, new Page.GetByTextOptions().setExact(true))))
                .locator(".field-error");
    }
}
