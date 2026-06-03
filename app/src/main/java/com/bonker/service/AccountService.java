package com.bonker.service;

import java.util.Map;
import java.util.Objects;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.bonker.exception.AccountNotFoundException;
import com.bonker.exception.InsufficientFundsException;
import com.bonker.model.Account;
import com.bonker.model.Card;
import com.bonker.model.Currency;
import com.bonker.model.SavingsAccount;
import com.bonker.model.Transaction;
import com.bonker.model.TransactionType;

public class AccountService {
    private static AccountService instance;

    private final Map<String, Account> accounts = new HashMap<>();

    private AccountService() {}

    public static AccountService getInstance() {
        if (instance == null)
            instance = new AccountService();
        return instance;
    }

    public void addAccount(Account account) {
        accounts.put(account.getIban(), account);
    }

    public void deposit(String iban, BigDecimal amount) {
        Account account = accounts.get(iban);
        if (account == null) {
            throw new AccountNotFoundException("Could not find account: " + iban);
        }
        account.setBalance(account.getBalance().add(amount));
        account.getTransactions().add(new Transaction(TransactionType.DEPOSIT, amount, account.getCurrency()));
    }

    public void withdraw(String iban, BigDecimal amount) throws InsufficientFundsException {
        Account account = accounts.get(iban);
        if (account == null) {
            throw new AccountNotFoundException("Could not find account: " + iban);
        }
        if (account.getBalance().compareTo(amount) == -1) {
            throw new InsufficientFundsException("Not enough money in account: " + iban);
        }
        account.setBalance(account.getBalance().subtract(amount));
        account.getTransactions().add(new Transaction(TransactionType.WITHDRAWAL, amount, account.getCurrency()));
    }

    public void transfer(String srcIban, String dstIban, BigDecimal amount) throws InsufficientFundsException {
        Account source = accounts.get(srcIban);
        Account destination = accounts.get(dstIban);

        if (source == null) {
            throw new AccountNotFoundException("Source account not found: " + srcIban);
        }
        if (destination == null) {
            throw new AccountNotFoundException("Destination account not found: " + dstIban);
        }
        if (!source.getCurrency().equals(destination.getCurrency())) {
            throw new IllegalArgumentException("Currencies do not match for direct transfer");
        }
        withdraw(srcIban, amount);
        deposit(dstIban, amount);
    }

    public void closeAccount(String iban) {
        Account account = accounts.get(iban);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + iban);
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot close account with non-zero balance: " + iban);
        }
        accounts.remove(iban);
    }

    public void applyInterest() {
        for (Account account : accounts.values()) {
            if (account instanceof SavingsAccount savings) {
                BigDecimal interest = savings.getBalance().multiply(savings.getInterestRate());
                deposit(savings.getIban(), interest);
            }
        }
    }

    public Account getAccount(String iban) {
        return accounts.get(iban);
    }

    public Map<String, Account> getAllAccounts() {
        return accounts;
    }

    public List<Account> getAccountsSortedByBalance(List<Account> clientAccounts) {
        List<Account> sorted = new ArrayList<>(clientAccounts);
        sorted.sort((a1, a2) -> -a1.getBalance().compareTo(a2.getBalance()));
        return sorted;
    }

    public BigDecimal calculateTotalBalance(List<Account> clientAccounts) {
        return clientAccounts.stream().map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void exchangeCurrency(String iban, Currency newCurrency, BigDecimal exchangeRate) {
        Account account = accounts.get(iban);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + iban);
        }
        account.setCurrency(newCurrency);
        BigDecimal newBalance = account.getBalance().multiply(exchangeRate);
        account.setBalance(newBalance);
        account.getTransactions().add(new Transaction(TransactionType.EXCHANGE, newBalance, newCurrency));
    }

    public void attachCard(String iban, Card card) {
        Account account = accounts.get(iban);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + iban);
        }
        account.getCards().add(card);
    }

    public void removeCard(String iban, String cardNumber) {
        Account account = accounts.get(iban);
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + iban);
        }
        account.getCards().removeIf(card ->
            Objects.equals(card.getNumber(), cardNumber)
        );
    }
}

