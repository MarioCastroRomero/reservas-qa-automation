package com.mariocastro.reservas.e2e.steps;

import com.mariocastro.reservas.e2e.PlaywrightHooks;
import com.mariocastro.reservas.e2e.pages.RegisterPage;
import com.mariocastro.reservas.e2e.UiState;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.mariocastro.reservas.e2e.support.TestUsers.generateRandomEmail;

public class RegisterUiSteps {

    private RegisterPage registerPage() {
        return new RegisterPage(PlaywrightHooks.page());
    }

    @Dado("que la persona está en la pantalla de registro")
    public void isOnRegisterScreen() {
        registerPage().open();
    }

    @Cuando("rellena el registro con datos válidos y un correo nuevo")
    public void fillsValidRegistration() {
       registerPage().register("Juana","Prueba", generateRandomEmail(),"1999-11-12","Prueba123456*", "Prueba123456*");
    }

    @Cuando("envía el formulario de registro sin rellenar nada")
    public void submitsEmptyRegistration() {
        registerPage().submit();
    }

    @Cuando("rellena el registro con la clave {string}")
    public void fillsRegistrationWithPassword(String password) {
        registerPage().register("Juana","Prueba", generateRandomEmail(),"1999-11-12", password, password);
    }

    @Cuando("rellena el registro con claves distintas en {string} y {string}")
    public void fillsMismatchedPasswords(String password, String confirmPassword) {
        registerPage().register("Juana","Prueba", generateRandomEmail(),"1999-11-12", password, confirmPassword);
    }

    @Cuando("la persona intenta registrarse con el correo de esa cuenta")
    public void registersWithExistingEmail() {
        registerPage().register("Juana","Prueba", UiState.user().email(),"1999-11-12", "Prueba123456*", "Prueba123456*");
    }

    @Entonces("el campo {string} muestra el error {string}")
    public void fieldShowsMessage(String field, String message) {
        assertThat(registerPage().fieldError(field)).containsText(message);
    }

    @Entonces("el campo {string} muestra un error")
    public void fieldShowsError(String field) {
        assertThat(registerPage().fieldError(field)).isVisible();
    }
}
