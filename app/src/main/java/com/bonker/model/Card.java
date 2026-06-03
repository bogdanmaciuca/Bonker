package com.bonker.model;

public class Card {
    private String number;
    private String expDate;
    private String cvv;
    private String accountIban;

    public Card(String number, String expDate, String cvv, String accountIban) {
        this.number      = number;
        this.expDate     = expDate;
        this.cvv         = cvv;
        this.accountIban = accountIban;
    }

    public String getNumber() { return number; }
    public String getAccountIban() { return accountIban; }
}

