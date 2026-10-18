package com.mariocastro.reservas.e2e;

public final class UiConfig {

    private UiConfig() {
    }

    public static String baseUrl() {
        return System.getProperty("e2e.baseUrl", "http://localhost:5174");
    }

    public static String apiUrl() {
        return System.getProperty("e2e.apiUrl", "http://localhost:8081");
    }

    public static String dbUrl() {
        return System.getProperty("e2e.dbUrl", "jdbc:postgresql://localhost:5432/reserva_test_db");
    }

    public static String dbUser() {
        return System.getProperty("e2e.dbUser", "postgres");
    }

    public static String dbPassword() {
        return System.getProperty("e2e.dbPassword", System.getenv().getOrDefault("DB_PASSWORD", "password123"));
    }

    public static boolean headless() {
        return Boolean.parseBoolean(System.getProperty("headless", "true"));
    }

    public static int slowMo() {
        return Integer.getInteger("slowMo", 0);
    }
}
