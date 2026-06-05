package com.bonker.repository;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
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
            ps.setString(2, entity.getClientId());
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
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public List<Account> findByClientId(String clientId) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT * FROM account WHERE client_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clientId);
            ResultSet rs = ps.executeQuery();
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

    public List<String> findAccountCardCounts() {
        List<String> result = new ArrayList<>();
        String sql = "SELECT a.iban, a.balance, a.currency, COUNT(c.number) AS card_count "
                   + "FROM account a LEFT JOIN card c ON a.iban = c.account_iban "
                   + "GROUP BY a.iban";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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

    private Account map(ResultSet rs) throws SQLException {
        Currency currency = new Currency(rs.getString("currency"), rs.getString("currency"));
        BigDecimal balance = BigDecimal.valueOf(rs.getDouble("balance"));
        String iban = rs.getString("iban");
        String type = rs.getString("type");

        Account acc = switch (type) {
            case "SAVINGS" -> new SavingsAccount(iban, currency, BigDecimal.valueOf(rs.getDouble("interest")), balance);
            case "FIXED_TERM_SAVINGS" -> new FixedTermSavingsAccount(iban, currency, balance);
            default -> new CheckingAccount(iban, currency, balance);
        };
        acc.setClientId(rs.getString("client_id"));
        return acc;
    }
}

