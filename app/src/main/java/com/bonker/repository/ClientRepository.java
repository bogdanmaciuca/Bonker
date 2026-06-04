package com.bonker.repository;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.bonker.model.Client;
import com.bonker.util.DatabaseConnection;

public class ClientRepository implements Repository<Client, String> {
    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    @Override
    public void save(Client entity) {
        String sql = "INSERT INTO client (id_number, first_name, last_name) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getIdNumber());
            ps.setString(2, entity.getFirstName());
            ps.setString(3, entity.getLastName());
            ps.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Client> findById(String id) {
        String sql = "SELECT * FROM client WHERE id_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Client> findAll() {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM client";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public void update(Client entity) {
        String sql = "UPDATE client SET first_name = ?, last_name = ? WHERE id_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getFirstName());
            ps.setString(2, entity.getLastName());
            ps.setString(3, entity.getIdNumber());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM client WHERE id_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> findClientAccountCounts() {
        List<String> result = new ArrayList<>();
        String sql = "SELECT c.first_name, c.last_name, c.id_number, COUNT(a.iban) AS account_count "
        + "FROM client c LEFT JOIN account a ON c.id_number = a.client_id "
        + "GROUP BY c.id_number";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(rs.getString("first_name") + " " + rs.getString("last_name")
                    + " (" + rs.getString("id_number") + ") — "
                    + rs.getInt("account_count") + " account(s)");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private Client map(ResultSet rs) throws SQLException {
        return new Client(rs.getString("first_name"), rs.getString("last_name"), rs.getString("id_number"));
    }
}

