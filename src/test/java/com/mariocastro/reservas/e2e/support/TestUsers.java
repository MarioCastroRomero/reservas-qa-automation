package com.mariocastro.reservas.e2e.support;

import com.mariocastro.reservas.e2e.UiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class TestUsers {

    public static final String VALID_PASSWORD = "Valida123!";

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final ObjectMapper JSON = new ObjectMapper();

    private TestUsers() {
    }

    public record User(String email, String password, String name, String token) {
    }

    public static User register(String name) {
        String email = generateRandomEmail();
        JsonNode response = post("/auth/register", null, Map.of(
                "name", name,
                "lastName", "Pérez",
                "email", email,
                "password", VALID_PASSWORD,
                "birthDate", "1990-05-20"));
        return new User(email, VALID_PASSWORD, name, response.get("token").asText());
    }

    public static String generateRandomEmail() {
        return "ui." + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    public static void createBusiness(User owner, String name) {
        post("/business", owner.token(), Map.of(
                "name", name,
                "cif", "B" + String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000)),
                "address", "Calle Mayor 1"));
    }

    private static JsonNode post(String path, String token, Object body) {
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(UiConfig.apiUrl() + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body)));
            if (token != null) {
                request.header("Authorization", "Bearer " + token);
            }
            HttpResponse<String> response = HTTP.send(request.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException("POST " + path + " -> " + response.statusCode() + ": " + response.body());
            }
            return JSON.readTree(response.body());
        } catch (IOException e) {
            throw new IllegalStateException("API not reachable at " + UiConfig.apiUrl() + " (is `make run` up?)", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
