package com.bonker.util;

import java.io.InputStream;
import java.util.Properties;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private DatabaseConnection() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            Properties props = new Properties();
            props.load(in);
            String url = props.getProperty("db.url");
            connection = DriverManager.getConnection(url);
            runSchema();
        }
        catch (Exception e) {
            throw new RuntimeException("DB initialization failed", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private void runSchema() throws SQLException {
        // skip if tables already exist (data persisted from previous run)
        try (Statement check = connection.createStatement()) {
            check.execute("SELECT 1 FROM client LIMIT 1");
            return;
        } catch (SQLException e) {
            // tables don't exist — create them
        }

        try (InputStream in = getClass().getClassLoader().getResourceAsStream("schema.sql");
             Statement stmt = connection.createStatement()) {
            String sql = new String(in.readAllBytes());
            for (String s : sql.split(";")) {
                if (!s.isBlank()) stmt.execute(s);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Schema init failed", e);
        }
    }
}

