package com.bonker.service;

import java.util.Objects;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;

import com.bonker.exception.AccountNotFoundException;
import com.bonker.exception.InsufficientFundsException;
import com.bonker.model.Account;
import com.bonker.model.Card;
import com.bonker.model.Currency;
import com.bonker.model.SavingsAccount;
import com.bonker.model.Transaction;
import com.bonker.model.TransactionType;
import com.bonker.repository.AccountRepository;
import com.bonker.util.DatabaseConnection;

public class AccountService {
    private static AccountService instance;

    private final AccountRepository accountRepository = new AccountRepository();

    private AccountService() {}

    public static AccountService getInstance() {
        if (instance == null)
            instance = new AccountService();
        return instance;
    }

    public void addAccount(Account account) {
        accountRepository.save(account);
    }

    public void deposit(String iban, BigDecimal amount) {
        var account = accountRepository.findById(iban);
        if (account.isEmpty()) {
            throw new AccountNotFoundException("Could not find account: " + iban);
        }
        Account acc = account.get();
        acc.setBalance(acc.getBalance().add(amount));
        acc.getTransactions().add(new Transaction(TransactionType.DEPOSIT, amount, acc.getCurrency()));
        accountRepository.update(acc);
    }

    public void withdraw(String iban, BigDecimal amount) throws InsufficientFundsException {
        var account = accountRepository.findById(iban);
        if (account.isEmpty()) {
            throw new AccountNotFoundException("Could not find account: " + iban);
        }
        Account acc = account.get();
        if (acc.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Not enough money in account: " + iban);
        }
        acc.setBalance(acc.getBalance().subtract(amount));
        acc.getTransactions().add(new Transaction(TransactionType.WITHDRAWAL, amount, acc.getCurrency()));
        accountRepository.update(acc);
    }

    public void transfer(String srcIban, String dstIban, BigDecimal amount) throws InsufficientFundsException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try {
            conn.setAutoCommit(false);

            var source = accountRepository.findById(srcIban);
            var destination = accountRepository.findById(dstIban);
            if (source.isEmpty() || destination.isEmpty()) {
                throw new AccountNotFoundException("Account not found");
            }
            if (!source.get().getCurrency().equals(destination.get().getCurrency())) {
                throw new IllegalArgumentException("Currencies do not match");
            }

            source.get().setBalance(source.get().getBalance().subtract(amount));
            destination.get().setBalance(destination.get().getBalance().add(amount));

            accountRepository.update(source.get());
            accountRepository.update(destination.get());

            conn.commit();
        } catch (Exception e) {
            try { conn.rollback(); } catch (SQLException ex) { throw new RuntimeException(ex); }
            if (e instanceof InsufficientFundsException ife) throw ife;
            if (e instanceof AccountNotFoundException anfe) throw anfe;
            if (e instanceof IllegalArgumentException iae) throw iae;
            throw new RuntimeException(e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { throw new RuntimeException(e); }
        }
    }

    public void closeAccount(String iban) {
        var account = accountRepository.findById(iban);
        if (account.isEmpty()) {
            throw new AccountNotFoundException("Account not found: " + iban);
        }
        if (account.get().getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot close account with non-zero balance: " + iban);
        }
        accountRepository.delete(iban);
    }

    public void applyInterest() {
        for (Account account : accountRepository.findAll()) {
            if (account instanceof SavingsAccount savings) {
                BigDecimal interest = savings.getBalance().multiply(savings.getInterestRate());
                deposit(savings.getIban(), interest);
            }
        }
    }

    public List<Account> getAccountsByClientId(String clientId) {
        return accountRepository.findByClientId(clientId);
    }

    public Account getAccount(String iban) {
        var account = accountRepository.findById(iban);
        if (account.isEmpty()) {
            throw new AccountNotFoundException("Could not find account: " + iban);
        }
        return account.get();
    }

    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    public List<Account> getAccountsSortedByBalance(List<Account> clientAccounts) {
        List<Account> sorted = new ArrayList<>(clientAccounts);
        sorted.sort((a1, a2) -> -a1.getBalance().compareTo(a2.getBalance()));
        return sorted;
    }

    public BigDecimal calculateTotalBalance(List<Account> clientaccountRepository) {
        return clientaccountRepository.stream().map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void exchangeCurrency(String iban, Currency newCurrency, BigDecimal exchangeRate) {
        var account = accountRepository.findById(iban);
        if (account.isEmpty()) {
            throw new AccountNotFoundException("Account not found: " + iban);
        }
        Account acc = account.get();
        acc.setCurrency(newCurrency);
        BigDecimal newBalance = acc.getBalance().multiply(exchangeRate);
        acc.setBalance(newBalance);
        acc.getTransactions().add(new Transaction(TransactionType.EXCHANGE, newBalance, newCurrency));
        accountRepository.update(acc);
    }

    public void attachCard(String iban, Card card) {
        var account = accountRepository.findById(iban);
        if (account.isEmpty()) {
            throw new AccountNotFoundException("Account not found: " + iban);
        }
        account.get().getCards().add(card);
    }

    public void removeCard(String iban, String cardNumber) {
        var account = accountRepository.findById(iban);
        if (account.isEmpty()) {
            throw new AccountNotFoundException("Account not found: " + iban);
        }
        account.get().getCards().removeIf(card ->
            Objects.equals(card.getNumber(), cardNumber)
        );
    }
}

