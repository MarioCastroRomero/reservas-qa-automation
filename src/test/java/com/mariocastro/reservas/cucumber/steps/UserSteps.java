package com.mariocastro.reservas.cucumber.steps;

import com.mariocastro.reservas.dto.ApiError;
import com.mariocastro.reservas.dto.UserDto;
import com.mariocastro.reservas.exception.ValidateException;
import com.mariocastro.reservas.model.Role;
import com.mariocastro.reservas.repository.UserRepository;
import com.mariocastro.reservas.service.AuthService;
import com.mariocastro.reservas.service.UserService;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserSteps {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @LocalServerPort
    private int port;

    private UserDto userDto;

    private Response response;

    private String token;

    private ApiError apiError;

    private UserDto userResponse;

    private String requestedUuid;

    @Before("@User")
    public void limpiarCuentas() {
        userRepository.deleteAll();
    }

    private ApiError apiError() {
        if (apiError == null) {
            apiError = response.as(ApiError.class);
        }
        return apiError;
    }

    private UserDto responseUser() {
        if (userResponse == null) {
            userResponse = response.as(UserDto.class);
        }
        return userResponse;
    }

    @Dado("que el servicio de usuarios está disponible")
    public void usuarioDisponible() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    @Dado("un usuario existente con UUID conocido")
    public void usuarioExistenteConocido() throws ValidateException {
        userDto = guardaUsuarioNormal("prueba@email.com", "Password12@");
        requestedUuid = userDto.getUuid().toString();
    }

    @Dado("un UUID {string} que no corresponde a ningún usuario")
    public void uuidUsuarioNoExiste(String uuid) {
        requestedUuid = uuid;
    }

    @Cuando("se envía una petición GET a {string} con ese UUID")
    public void seEnviaGetUuid(String path) {
        response = RestAssured.given()
                .auth()
                .oauth2(token)
                .contentType(ContentType.JSON)
                .when()
                .get(path, Map.of("uuid", requestedUuid));
    }

    @Cuando("se envía una petición GET a {string} sin cabecera \"Authorization\"")
    public void peticionGetSinAutorizacion(String path) {
        response = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .when()
                .get(path, Map.of("uuid", requestedUuid));
    }

    @Entonces("la respuesta debe tener el código de estado {int}")
    public void codigoDeEstado(int code) {
        response.then().statusCode(code);
    }

    @Y("una petición autenticada con un token JWT válido")
    public void peticionAtenticadaJWT() throws ValidateException {
        UserDto loginUser = guardaUsuarioAdmin("admina@email.com", "Password12@");
        token = authService.generateToken(loginUser);
    }

    @Y("la respuesta debe contener los datos del usuario: uuid, nombre, apellidos, correo y fecha de nacimiento")
    public void contieneLosDatosDelUsuario() {
        UserDto responseUser = responseUser();
        assertEquals(userDto.getName(), responseUser.getName());
        assertEquals(userDto.getLastName(), responseUser.getLastName());
        assertEquals(userDto.getEmail(), responseUser.getEmail());
        assertEquals(userDto.getBirthDate(), responseUser.getBirthDate());
        response.then().body("uuid", Matchers.equalTo(userDto.getUuid().toString()));
    }

    @Y("la respuesta no debe incluir la contraseña")
    public void noIncluyeContraseña() {
        assertNull(responseUser().getPassword());
    }

    @Y("la respuesta no debe incluir datos del usuario")
    public void noIncluyeDatosDeUsuario() {
        String body = response.asString();
        assertFalse(body.contains(userDto.getUuid().toString()));
        assertFalse(body.contains(userDto.getEmail()));
    }

    @Y("la respuesta debe dar error de autenticacion")
    public void errorDeAutentificacion() {
        assertEquals("Unauthorized", apiError().error());
        assertEquals(401, apiError().status());
    }

    @Y("el mensaje de error debe indicar {string}")
    public void mensajeDeError(String error) {
        assertEquals(error, apiError().error());
    }

    private UserDto guardaUsuarioNormal(String email, String pwd) throws ValidateException {
        UserDto dto = createUser(email, pwd);
        return userService.saveUser(dto);
    }
    private UserDto guardaUsuarioAdmin(String email, String pwd) throws ValidateException {
        UserDto dto = createUser(email, pwd);
        return userService.saveUserAsRole(dto, Role.ADMIN);
    }

    private static UserDto createUser(String email, String pwd) {
        UserDto dto = new UserDto();
        dto.setEmail(email);
        dto.setPassword(pwd);
        dto.setName("Usuario");
        dto.setLastName("Prueba");
        dto.setBirthDate(LocalDate.of(1990, 1, 1));
        return dto;
    }

}
