package com.mariocastro.reservas.cucumber.steps;

import com.mariocastro.reservas.dto.UserDto;
import com.mariocastro.reservas.model.Booking;
import com.mariocastro.reservas.model.BookingStatus;
import com.mariocastro.reservas.model.Business;
import com.mariocastro.reservas.model.Role;
import com.mariocastro.reservas.model.User;
import com.mariocastro.reservas.repository.BookingRepository;
import com.mariocastro.reservas.repository.BusinessRepository;
import com.mariocastro.reservas.repository.UserRepository;
import com.mariocastro.reservas.service.AuthService;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class BusinessSteps {

    @LocalServerPort
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private BookingRepository bookingRepository;

    private Response response;

    private User owner;

    private User otherOwner;

    private User normalUser;

    private User adminUser;

    private String token;

    private User loginUSer;

    private Business empresaSeleccionada;

    private Business empresaBody;

    private String uuid;

    private Map<String, String> pathParam;

    private List<Booking> reservasEmpresa;

    @Dado("que el servicio de empresas está disponible")
    public void empresaDisponible() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.basePath = "";
        bookingRepository.deleteAll();
        businessRepository.deleteAll();
        userRepository.deleteAll();
        pathParam = new HashMap<>();
        owner = guardaUsuario("pepe", "propietario", "propietario@prueba.com", "Password123@", LocalDate.of(1995, 5, 15), Role.BUSINESS_OWNER);
        otherOwner = guardaUsuario("Pedro", "otro propietario", "otroPropietario@prueba.com", "Password123@", LocalDate.of(1995, 5, 12), Role.BUSINESS_OWNER);
        normalUser = guardaUsuario("Mario", "usuario normal", "usuarioNormal@prueba.com", "Password123", LocalDate.of(1995, 5, 12), Role.USER);
        adminUser = guardaUsuario("Admin", "Admin", "admin@prueba.com", "Password123", LocalDate.of(1995, 5, 12), Role.ADMIN);
    }

    @Dado("el catálogo de empresas de ejemplo")
    public void catalogoEmpresasEjemplo() {
        crearCatalogo();
    }

    @Dado("sin cabecera \"Authorization\"")
    public void cabeceraSinAutorizacion() {
        token = null;
    }

    @Dado("una empresa inactiva cuyo dueño es {string}")
    public void empresaInactivaCuyoDuenyo(String duenyo) {
        User user;
        if ("otro".equals(duenyo)) {
            user = otherOwner;
        } else {
            user = owner;
        }
        empresaSeleccionada = guardaEmpresa(user, "Peluquería Ana", "Calle Mayor 1, Madrid", false, "A28000016");
        uuid = empresaSeleccionada.getUuid().toString();
    }

    @Dado("una empresa activa cuyo dueño es {string}")
    public void empresaActivaCuyoDuenyo(String duenyo) {
        User user;
        if ("otro".equals(duenyo)) {
            user = otherOwner;
        } else {
            user = owner;
        }
        empresaSeleccionada = guardaEmpresa(user, "Peluquería Ana 2", "Calle Mayor 1, Madrid", true, "A28000017");
        uuid = empresaSeleccionada.getUuid().toString();
    }

    @Dado("una empresa inexistente con UUID {string}")
    public void empresaInexistente(String uuidStr) {
        uuid = uuidStr;
    }

    @Dado("los datos de una empresa válida")
    public void datosEmpresaValida() {
        empresaBody = creaEmpresa(loginUSer, "Peluquería Ana 3", "Calle Mayor 1, Madrid", true, "A28000018");
    }

    @Dado("los datos de una empresa con un CIF ya registrado")
    public void empresaConCifRegistrado() {
        String cif = "A28000018";
        guardaEmpresa(owner, "Peluquería Ana 2", "Calle Mayor 1, Madrid", true, cif);
        empresaBody = creaEmpresa(loginUSer, "Peluquería Ana 3", "Calle Mayor 1, Madrid", true, cif);
    }

    @Dado("los datos de una empresa con un CIF {string}")
    public void empresaConCif(String cif) {
        empresaBody = creaEmpresa(loginUSer, "Peluquería Ana 3", "Calle Mayor 1, Madrid", true, cif);
    }

    @Dado("los datos de una empresa válida con {string} igual a {string}")
    public void empresaValidaConIgualA(String campo, String valor) {
        empresaBody = creaEmpresa(loginUSer, "Peluquería Ana 3", "Calle Mayor 1, Madrid", true, "A28000018");
        switch (campo) {
            case "name" -> empresaBody.setName(valor);
            case "address" -> empresaBody.setAddress(valor);
            case "cif" -> empresaBody.setCif(valor);
            default -> throw new IllegalArgumentException("Campo no soportado: " + campo);
        }
    }

    @Dado("los datos de una empresa con un nombre de {int} caracteres")
    public void empresaConNombreDeXCaracteres(Integer longitud) {
        empresaBody = creaEmpresa(loginUSer, "Peluquería Ana 3", "Calle Mayor 1, Madrid", true, "A28000018");
        empresaBody.setName("a".repeat(longitud));
    }

    @Dado("los datos de una empresa vacíos")
    public void empresaVacia() {
        empresaBody = new Business();
    }

    @Dado("que tiene reservas activas")
    public void tieneReservasActivas() {
        reservasEmpresa = List.of(
                guardaReserva(normalUser, empresaSeleccionada, BookingStatus.PENDING, 1),
                guardaReserva(normalUser, empresaSeleccionada, BookingStatus.CONFIRMED, 2));
    }

    @Dado("que tiene reservas")
    public void tieneReservas() {
        reservasEmpresa = List.of(
                guardaReserva(normalUser, empresaSeleccionada, BookingStatus.PENDING, 1),
                guardaReserva(normalUser, empresaSeleccionada, BookingStatus.CONFIRMED, 2),
                guardaReserva(normalUser, empresaSeleccionada, BookingStatus.CANCELLED, 3),
                guardaReserva(normalUser, empresaSeleccionada, BookingStatus.COMPLETED, 4));
    }

    @Cuando("se envía una petición {word} a {string}")
    public void executeRequest(String metodo, String path) {
        RequestSpecification requestSpecification = RestAssured.given();
        if (token != null) {
            requestSpecification = requestSpecification.auth().oauth2(token);
        }
        requestSpecification = requestSpecification
                .contentType(ContentType.JSON)
                .params(pathParam)
                .when();

        if (empresaBody != null) {
            requestSpecification.body(empresaBody);
        }

        if (uuid != null && path.contains("{uuid}")) {
            response = requestSpecification.request(metodo, path, uuid);
        } else {
            response = requestSpecification.request(metodo, path);
        }
    }

    @Entonces("la respuesta tiene el estado {int}")
    public void codigoRespuesta(int code) {
        response.then().statusCode(code);
    }

    @Entonces("la respuesta debe contener la empresa solicitada")
    public void debeContenerEmpresa() {
        assertEquals(empresaSeleccionada.getUuid().toString(), response.jsonPath().getString("uuid"));
        assertEquals(empresaSeleccionada.getName(), response.jsonPath().getString("name"));
    }

    @Entonces("el details debe contener {string}")
    public void detallesContiene(String detalles) {
        response.then().body("details", Matchers.hasItem(Matchers.equalTo(detalles)));
    }

    @Entonces("la respuesta debe contener las empresas pertenecientes del usuario")
    public void respuestaConEmpresasDelUsuario() {
        String uuidUser = loginUSer.getUuid().toString();
        response.then().body("ownerId", Matchers.everyItem(Matchers.equalTo(uuidUser)));
    }

    @Entonces("la respuesta debe contener la empresa")
    public void respuestaContieneEmpresa() {
        assertNotNull(response.jsonPath().getString("uuid"));
        assertEquals(empresaBody.getName(), response.jsonPath().getString("name"));
        assertEquals(empresaBody.getAddress(), response.jsonPath().getString("address"));
        assertEquals(empresaBody.getCif(), response.jsonPath().getString("cif"));
    }

    @Entonces("mi rol debe seguir siendo el mismo")
    public void rolDebeSerElMismo() {
        Optional<User> optionalUser = userRepository.findById(loginUSer.getUuid());
        assertTrue(optionalUser.isPresent());
        User userBd = optionalUser.get();
        assertEquals(loginUSer.getRole(), userBd.getRole());
    }

    @Entonces("mi rol debe haber cambiado a {string}")
    public void rolDebeCambiar(String role) {
        Optional<User> optionalUser = userRepository.findById(loginUSer.getUuid());
        assertTrue(optionalUser.isPresent());
        User userBd = optionalUser.get();
        assertNotEquals(loginUSer.getRole(), userBd.getRole());
        assertEquals(role, userBd.getRole().name());
    }

    @Entonces("la respuesta debe estar vacia")
    public void respuestaDebeEStarVacia() {
        assertTrue(response.getBody().asString().isEmpty());
    }

    @Entonces("la empresa debe haber quedado inactiva")
    public void empresaInactiva() {
        Optional<Business> optionalBusiness = businessRepository.findById(empresaSeleccionada.getUuid());
        assertTrue(optionalBusiness.isPresent());
        assertFalse(optionalBusiness.get().isActive());
    }

    @Entonces("la respuesta debe contener todas las reservas de la empresa")
    public void respuestaContieneReservasEmpresa() {
        compruebaReservasEmpresa(reservasEmpresa);
    }

    @Entonces("la respuesta debe contener todas las reservas de la empresa con status {string}")
    public void respuestaContieneReservasEmpresaConStatus(String status) {
        BookingStatus bookingStatus = BookingStatus.valueOf(status);
        List<Booking> esperadas = reservasEmpresa.stream()
                .filter(booking -> booking.getStatus() == bookingStatus)
                .toList();
        compruebaReservasEmpresa(esperadas);
        response.then().body("status", Matchers.everyItem(Matchers.equalTo(status)));
    }

    @Entonces("la respuesta debe contener {int} elementos")
    public void respuestaContieneElementos(int elementos) {
        assertEquals(elementos, response.jsonPath().getList("$").size(), response.asString());
    }

    @Y("una petición autenticada con rol {string}")
    public void peticionAutenticadaRol(String rol) {
        Role role = Role.valueOf(rol);
        loginUSer = switch (role) {
            case USER -> normalUser;
            case ADMIN -> adminUser;
            case BUSINESS_OWNER -> owner;
        };
        token = authService.generateToken(UserDto.fromUser(loginUSer));
    }

    @Y("la respuesta debe contener exactamente las empresas {string}")
    public void respuestaContieneEmpresas(String empresasStr) {
        List<String> empresas = Arrays.stream(empresasStr.split(",")).map(String::trim).toList();
        assertTrue(empresas.containsAll(response.jsonPath().getList("name", String.class)));
    }

    @Y("el parámetro {string} con valor {string}")
    public void incluyeValor(String clave, String valor) {
        pathParam.put(clave, valor);
    }

    @Y("la respuesta debe contener las empresas en este orden {string}")
    public void respuestaContieneEmpresasEnOrden(String empresasStr) {
        List<String> empresas = Arrays.stream(empresasStr.split(",")).map(String::trim).toList();
        assertEquals(empresas, response.jsonPath().getList("name", String.class));
    }

    @Y("la respuesta debe ser una lista vacía")
    public void respuestaContieneListaVacia() {
        assertTrue(response.jsonPath().getList("$").isEmpty(), response.asString());
    }

    @Y("la respuesta es un error de autenticación")
    public void respuestaErrorAutentificacion() {
        JsonPath jsonPath = response.jsonPath();
        assertEquals("Unauthorized", jsonPath.get("error"));
    }

    @Y("el error debe indicar {string}")
    public void errorDebeIndicar(String error) {
        JsonPath jsonPath = response.jsonPath();
        assertEquals(error, jsonPath.get("error"));
    }

    public void crearCatalogo() {
        guardaEmpresa(owner, "Peluquería Ana", "Calle Mayor 1, Madrid", true, "A28000016");
        guardaEmpresa(owner, "Barbería Centro", "Plaza España 3, Sevilla", true, "B08123457");
        guardaEmpresa(owner, "Peluquería Cerrada", "Calle Mayor 9, Madrid", false, "G41987652");
        guardaEmpresa(owner, "Clínica Dental Sol", "Avenida Sol 5, Madrid", true, "F46543211");
        guardaEmpresa(otherOwner, "Peluquería PEEP", "Calle Mayor 4, Madrid", true, "H29111119");
        guardaEmpresa(otherOwner, "Barbería Centro REC", "Plaza España 7, Sevilla", true, "B28987659");
        guardaEmpresa(otherOwner, "Peluquería Cerrada REC", "Calle Mayor 8, Madrid", false, "Q0300123G");
        guardaEmpresa(otherOwner, "Clínica Dental Luna", "Avenida Luna 5, Madrid", true, "B12345674");

    }

    private User guardaUsuario(String name, String lastName, String email, String password, LocalDate birthdate, Role rol) {
        User user = new User();
        user.setName(name);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(password);
        user.setBirthDate(birthdate);
        user.setRole(rol);
        return userRepository.save(user);
    }

    private Business guardaEmpresa(User user, String name, String address, Boolean active, String cif) {
        Business business = creaEmpresa(user, name, address, active, cif);
        return businessRepository.save(business);
    }

    private static Business creaEmpresa(User user, String name, String address, Boolean active, String cif) {
        Business business = new Business();
        business.setOwner(user);
        business.setName(name);
        business.setAddress(address);
        business.setActive(active);
        business.setCif(cif);
        return business;
    }

    private Booking guardaReserva(User holder, Business business, BookingStatus status, int diasDesdeHoy) {
        Booking booking = new Booking();
        booking.setHolder(holder);
        booking.setBusiness(business);
        booking.setBookingTime(LocalDateTime.now().plusDays(diasDesdeHoy));
        booking.setPartySize(2);
        booking = bookingRepository.save(booking);
        if (status != BookingStatus.PENDING) {
            booking.setStatus(status);
            booking = bookingRepository.save(booking);
        }
        return booking;
    }

    private void compruebaReservasEmpresa(List<Booking> esperadas) {
        List<String> uuidsEsperados = esperadas.stream().map(booking -> booking.getUuid().toString()).toList();
        List<String> uuidsRespuesta = response.jsonPath().getList("uuid", String.class);
        assertEquals(uuidsEsperados.size(), uuidsRespuesta.size(), response.asString());
        assertTrue(uuidsRespuesta.containsAll(uuidsEsperados), response.asString());
        response.then().body("businessId", Matchers.everyItem(Matchers.equalTo(empresaSeleccionada.getUuid().toString())));
    }

}
