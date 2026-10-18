package com.mariocastro.reservas.cucumber.steps;

import com.mariocastro.reservas.dto.UserDto;
import com.mariocastro.reservas.exception.ValidateException;
import com.mariocastro.reservas.model.User;
import com.mariocastro.reservas.repository.UserRepository;
import com.mariocastro.reservas.service.UserService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class AuthSteps {

    private static final String CLAVE_VALIDA = "Valida123!";
    private static final String BCRYPT_PREFIJO = "$2";

    @LocalServerPort
    private int port;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private Response response;

    private Map<String, Object> datosRegistro;

    @Before("@Auth")
    public void limpiarCuentas() {
        userRepository.deleteAll();
    }

    @Dado("que el sistema de autenticación está operativo")
    public void sistemaAutenticacionOperativo() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "";
    }

    @Dado("que existe una cuenta con correo {string} y clave {string}")
    public void existeCuenta(String correo, String clave) throws ValidateException {
        guardarCuenta(correo, clave);
    }

    @Dado("que ya existe una cuenta con correo {string}")
    public void yaExisteCuenta(String correo) throws ValidateException {
        guardarCuenta(correo, CLAVE_VALIDA);
    }

    @Dado("los datos de registro de una persona nueva:")
    public void datosRegistroPersonaNueva(DataTable tabla) {
        Map<String, String> fila = tabla.asMaps().getFirst();
        datosRegistro = new LinkedHashMap<>(fila);
    }

    @Cuando("la persona inicia sesión en {string} con correo {string} y clave {string}")
    public void iniciarSesion(String endpoint, String correo, String clave) {
        Map<String, String> cuerpo = new LinkedHashMap<>();
        cuerpo.put("email", correo);
        cuerpo.put("password", clave);

        response = enviarPost(endpoint, cuerpo);
    }

    @Cuando("la persona se registra en {string} con los datos de registro")
    public void registrarConDatosTabla(String endpoint) {
        response = enviarPost(endpoint, datosRegistro);
    }

    @Cuando("la persona se registra en {string} con correo {string} y clave {string}")
    public void registrarConCorreoYClave(String endpoint, String correo, String clave) {
        datosRegistro = datosValidosRegistro();
        datosRegistro.put("email", correo);
        datosRegistro.put("password", clave);

        response = enviarPost(endpoint, datosRegistro);
    }

    @Cuando("la persona se registra en {string} con clave {string} y resto de datos válidos")
    public void registrarConClave(String endpoint, String clave) {
        datosRegistro = datosValidosRegistro();
        datosRegistro.put("password", clave);

        response = enviarPost(endpoint, datosRegistro);
    }

    @Cuando("la persona se registra en {string} con fecha de nacimiento {string}")
    public void registrarConFechaNacimiento(String endpoint, String fechaNacimiento) {
        datosRegistro = datosValidosRegistro();
        datosRegistro.put("birthDate", fechaNacimiento);

        response = enviarPost(endpoint, datosRegistro);
    }

    @Entonces("el sistema responde con estado {int}")
    public void elSistemaResponde(int estadoEsperado) {
        response.then().statusCode(estadoEsperado);
    }

    @Y("la respuesta contiene un token de tipo {string}")
    public void laRespuestaContieneTokenTipo(String tipoEsperado) {
        response.then()
                .body("token", notNullValue())
                .body("type", equalTo(tipoEsperado));
    }

    @Y("la respuesta contiene un token y el uuid de la cuenta creada")
    public void laRespuestaContieneTokenYUuid() {
        response.then()
                .body("token", notNullValue())
                .body("uuid", notNullValue());
    }

    @Y("la respuesta identifica al usuario con correo {string}")
    public void laRespuestaIdentificaUsuario(String correoEsperado) {
        response.then()
                .body("email", equalTo(correoEsperado))
                .body("name", notNullValue());
    }

    @Y("no se devuelve ningún token")
    public void noSeDevuelveToken() {
        response.then().body("token", nullValue());
    }

    @Y("el error es {string}")
    public void elErrorEs(String errorEsperado) {
        response.then().body("error", equalTo(errorEsperado));
    }

    @Y("el detalle del error menciona {string}")
    public void elDetalleMencionaTexto(String textoEsperado) {
        response.then().body("details", hasItem(containsString(textoEsperado)));
    }

    @Y("existe una cuenta guardada con correo {string}")
    public void existeCuentaGuardada(String correo) {
        Assertions.assertTrue(userRepository.findByEmail(correo).isPresent(),
                "Se esperaba una cuenta guardada con correo " + correo);
    }

    @Y("la clave guardada está cifrada y no coincide con la original")
    public void laClaveGuardadaEstaCifrada() {
        String correo = String.valueOf(datosRegistro.get("email"));
        String claveOriginal = String.valueOf(datosRegistro.get("password"));

        User usuario = userRepository.findByEmail(correo).orElseThrow();

        Assertions.assertNotEquals(claveOriginal, usuario.getPassword(),
                "La clave no debe guardarse en texto plano");
        Assertions.assertTrue(usuario.getPassword().startsWith(BCRYPT_PREFIJO),
                "La clave debe guardarse con BCrypt");
    }

    private void guardarCuenta(String correo, String clave) throws ValidateException {
        UserDto dto = new UserDto();
        dto.setEmail(correo);
        dto.setPassword(clave);
        dto.setName("Usuario");
        dto.setLastName("Prueba");
        dto.setBirthDate(LocalDate.of(1990, 1, 1));

        userService.saveUser(dto);
    }

    private static Map<String, Object> datosValidosRegistro() {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("name", "Ana");
        datos.put("lastName", "Pérez");
        datos.put("email", "nueva.persona@example.com");
        datos.put("password", CLAVE_VALIDA);
        datos.put("birthDate", "1990-01-01");
        return datos;
    }

    private Response enviarPost(String endpoint, Map<String, ?> cuerpo) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(cuerpo)
                .when()
                .post(endpoint);
    }
}
