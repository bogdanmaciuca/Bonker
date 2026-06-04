package com.bonker.repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import com.bonker.model.Account;
import com.bonker.model.CheckingAccount;
import com.bonker.model.Currency;
import com.bonker.model.FixedTermSavingsAccount;
import com.bonker.model.SavingsAccount;
import com.bonker.util.DatabaseConnection;

public class AccountRepository implements Repository<Account, String> {
    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    @Override
    public void save(Account entity) {
        String sql = "INSERT INTO account (iban, client_id, currency, balance, type, interest) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getIban());
            ps.setString(2, null);
            ps.setString(3, entity.getCurrency().code());
            ps.setDouble(4, entity.getBalance().doubleValue());
            ps.setString(5, entity.getType().name());
            double interest = (entity instanceof SavingsAccount s) ? s.getInterestRate().doubleValue() : 0;
            ps.setDouble(6, interest);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Account> findById(String id) {
        String sql = "SELECT * FROM account WHERE iban = ?";
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
    public List<Account> findAll() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM account";
        try (Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public void update(Account entity) {
        String sql = "UPDATE account SET currency = ?, balance = ?, interest = ? WHERE iban = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getCurrency().code());
            ps.setDouble(2, entity.getBalance().doubleValue());
            double interest = (entity instanceof SavingsAccount s) ? s.getInterestRate().doubleValue() : 0;
            ps.setDouble(3, interest);
            ps.setString(4, entity.getIban());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM account WHERE iban = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Account map(ResultSet rs) throws SQLException {
        Currency currency = new Currency(rs.getString("currency"), rs.getString("currency"));
        BigDecimal balance = BigDecimal.valueOf(rs.getDouble("balance"));
        String iban = rs.getString("iban");
        String type = rs.getString("type");

        return switch (type) {
            case "SAVINGS" -> new SavingsAccount(iban, currency, BigDecimal.valueOf(rs.getDouble("interest")), balance);
            case "FIXED_TERM_SAVINGS" -> new FixedTermSavingsAccount(iban, currency, balance);
            default -> new CheckingAccount(iban, currency, balance);
        };
    }
}

