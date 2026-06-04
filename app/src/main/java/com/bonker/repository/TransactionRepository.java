package com.bonker.repository;

import com.bonker.model.Currency;
import com.bonker.model.Transaction;
import com.bonker.model.TransactionType;
import com.bonker.util.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class TransactionRepository implements Repository<Transaction, Long> {
    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    @Override
    public void save(Transaction entity) {
        String sql = "INSERT INTO transaction (account_iban, type, amount, currency, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, null);
            ps.setString(2, entity.type().name());
            ps.setDouble(3, entity.amount().doubleValue());
            ps.setString(4, entity.currency().code());
            ps.setString(5, entity.timestamp().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Transaction> findById(Long id) {
        String sql = "SELECT * FROM transaction WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transaction";
        try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public void update(Transaction entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM transaction WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Transaction map(ResultSet rs) throws SQLException {
        return new Transaction(
            TransactionType.valueOf(rs.getString("type")),
            java.math.BigDecimal.valueOf(rs.getDouble("amount")),
            new Currency(rs.getString("currency"), rs.getString("currency")),
            LocalDateTime.parse(rs.getString("timestamp"))
        );
    }
}
