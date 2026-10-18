package com.mariocastro.reservas.e2e.steps;

import com.mariocastro.reservas.e2e.PlaywrightHooks;
import com.mariocastro.reservas.e2e.UiState;
import com.mariocastro.reservas.e2e.pages.LoginPage;
import com.mariocastro.reservas.e2e.support.TestUsers;
import com.microsoft.playwright.Page;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class CommonUiSteps {

    private static final String TOKEN_KEY = "reservas.token";
    private static final String USER_KEY = "reservas.user";

    private Page page() {
        return PlaywrightHooks.page();
    }

    @Dado("que existe una cuenta registrada con una clave válida")
    public void accountWithValidPassword() {
        UiState.setUser(TestUsers.register("Ana"));
    }

    @Dado("que hay una persona con sesión iniciada en la web")
    public void loggedInPerson() {
        accountWithValidPassword();
        LoginPage login = new LoginPage(page());
        login.open();
        login.logIn(UiState.user().email(), UiState.user().password());
        assertThat(page()).hasURL(Pattern.compile(".*/negocios"));
    }

    @Cuando("una persona sin sesión abre {string}")
    public void anonymousOpens(String path) {
        page().navigate(path);
    }

    @Entonces("la web muestra el catálogo de negocios")
    public void catalogIsShown() {
        assertThat(page()).hasURL(Pattern.compile(".*/negocios"));
    }

    @Entonces("la persona sigue en la pantalla de login")
    public void stillOnLogin() {
        assertThat(page()).hasURL(Pattern.compile(".*/login"));
    }

    @Entonces("la web muestra el error {string}")
    public void errorIsShown(String message) {
        assertThat(new LoginPage(page()).formError()).containsText(message);
    }

    @Entonces("la web marca errores en los campos del formulario")
    public void fieldsHaveErrors() {
        assertThat(new LoginPage(page()).fieldErrors().first()).isVisible();
    }
}
