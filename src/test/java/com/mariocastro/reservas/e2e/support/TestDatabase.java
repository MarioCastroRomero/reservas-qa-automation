package com.mariocastro.reservas.e2e.support;

import com.mariocastro.reservas.e2e.UiConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class TestDatabase {

    private TestDatabase() {
    }

    public static void clean() {
        if (!UiConfig.dbUrl().contains("test")) {
            throw new IllegalStateException("Refusing to wipe " + UiConfig.dbUrl() + ": its name must contain 'test'");
        }
        try (Connection connection = DriverManager.getConnection(UiConfig.dbUrl(), UiConfig.dbUser(), UiConfig.dbPassword());
             Statement statement = connection.createStatement()) {
            List<String> tables = new ArrayList<>();
            try (var rs = statement.executeQuery("SELECT quote_ident(tablename) FROM pg_tables WHERE schemaname = 'public'")) {
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
            }
            if (!tables.isEmpty()) {
                statement.execute("TRUNCATE TABLE " + String.join(", ", tables) + " RESTART IDENTITY CASCADE");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot clean " + UiConfig.dbUrl() + " (is `make run-test` up?)", e);
        }
    }
}
