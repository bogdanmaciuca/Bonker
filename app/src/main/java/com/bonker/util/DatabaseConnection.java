package com.bonker.util;

import java.io.InputStream;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
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

    public List<String> findAccountCardCounts() {
        List<String> result = new ArrayList<>();
        String sql = "SELECT a.iban, a.balance, a.currency, COUNT(c.number) AS card_count "
        + "FROM account a LEFT JOIN card c ON a.iban = c.account_iban "
        + "GROUP BY a.iban";
        try (Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(rs.getString("iban") + " | "
                    + rs.getDouble("balance") + " " + rs.getString("currency") + " | "
                    + rs.getInt("card_count") + " card(s)");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private void runSchema() throws SQLException {
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

