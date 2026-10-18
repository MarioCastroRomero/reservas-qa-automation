package com.mariocastro.reservas.e2e.steps;

import com.mariocastro.reservas.e2e.PlaywrightHooks;
import com.mariocastro.reservas.e2e.UiState;
import com.mariocastro.reservas.e2e.pages.LoginPage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Y;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class LoginUiSteps {

    private LoginPage loginPage() {
        return new LoginPage(PlaywrightHooks.page());
    }

    @Dado("que la persona está en la pantalla de login")
    public void isOnLoginScreen() {
        loginPage().open();
    }

    @Cuando("la persona inicia sesión en la web con esas credenciales")
    public void logsInWithCredentials() {
        loginPage().open();
        loginPage().logIn(UiState.user().email(), UiState.user().password());
    }

    @Cuando("la persona inicia sesión en la web con una clave incorrecta")
    public void logsInWithWrongPassword() {
        loginPage().open();
        loginPage().logIn(UiState.user().email(), "ClaveIncorrecta9!");
    }

    @Cuando("envía el formulario de login sin rellenar nada")
    public void submitsEmptyLogin() {
        loginPage().submit();
    }

    @Y("el saludo incluye el nombre {string}")
    public void greetingIncludesName(String name) {
        assertThat(PlaywrightHooks.page().getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1)))
                .containsText("Hola, " + name);
    }
}
