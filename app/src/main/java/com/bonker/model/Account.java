package com.bonker.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Account {
    protected String            iban;
    protected Currency          currency;
    protected BigDecimal        balance;
    protected List<Transaction> transactions;
    protected List<Card>        cards;

    public String getIban() { return iban; }
    public BigDecimal getBalance() { return balance; }
    public List<Transaction> getTransactions() { return transactions; }
    public Currency getCurrency() { return currency; }
    public List<Card> getCards() { return cards; }

    public void setIban(String iban) { this.iban = iban; }
    public void setCurrency(Currency currency) { this.currency = currency; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    public void setCards(List<Card> cards) { this.cards = cards; }

    public Account(String iban, Currency currency) {
        this.iban = iban;
        this.balance = new BigDecimal(0);
        this.currency = currency;
        this.transactions = new ArrayList<>();
        this.cards = new ArrayList<>();
    }

    public Account(String iban, Currency currency, BigDecimal balance) {
        this.iban = iban;
        this.balance = balance;
        this.currency = currency;
        this.transactions = new ArrayList<>();
        this.cards = new ArrayList<>();
    }

    public Account(String iban, Currency currency, double balance) {
        this.iban = iban;
        this.balance = new BigDecimal(balance);
        this.currency = currency;
        this.transactions = new ArrayList<>();
        this.cards = new ArrayList<>();
    }

    public abstract AccountType getType();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account client = (Account)o;
        return iban.equals(client.iban);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iban);
    }
}

