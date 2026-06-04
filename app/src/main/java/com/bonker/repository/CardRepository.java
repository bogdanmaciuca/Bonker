package com.bonker.repository;

import com.bonker.model.Card;
import com.bonker.util.DatabaseConnection;

import java.sql.Connection;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

public class CardRepository implements Repository<Card, String> {
    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    @Override
    public void save(Card entity) {
        String sql = "INSERT INTO card (number, exp_date, cvv, account_iban) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getNumber());
            ps.setString(2, null);
            ps.setString(3, null);
            ps.setString(4, entity.getAccountIban());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Card> findById(String id) {
        String sql = "SELECT * FROM card WHERE number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Card> findAll() {
        List<Card> list = new ArrayList<>();
        String sql = "SELECT * FROM card";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public void update(Card entity) {
        String sql = "UPDATE card SET exp_date = ?, cvv = ?, account_iban = ? WHERE number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, null);
            ps.setString(2, null);
            ps.setString(3, entity.getAccountIban());
            ps.setString(4, entity.getNumber());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM card WHERE number = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> findCardAccountDetails() {
        List<String> result = new ArrayList<>();
        String sql = "SELECT c.number, c.exp_date, a.iban, a.balance, a.currency "
        + "FROM card c JOIN account a ON c.account_iban = a.iban";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add("Card " + rs.getString("number") + " (exp " + rs.getString("exp_date") + ")"
                    + " -> " + rs.getString("iban") + " ["
                    + rs.getDouble("balance") + " " + rs.getString("currency") + "]");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private Card map(ResultSet rs) throws SQLException {
        return new Card(rs.getString("number"), rs.getString("exp_date"),
            rs.getString("cvv"), rs.getString("account_iban"));
    }
}
