package com.bonker.models;

import java.math.BigDecimal;

public class SavingsAccount extends Account {
    protected BigDecimal interestRate;

    public SavingsAccount(String iban, Currency currency, BigDecimal interestRate) {
        super(iban, currency);
        this.interestRate = interestRate;
    }

    public SavingsAccount(String iban, Currency currency, BigDecimal interestRate, BigDecimal balance) {
        super(iban, currency, balance);
        this.interestRate = interestRate;
    }

    public SavingsAccount(String iban, Currency currency, double interestRate, double balance) {
        super(iban, currency, balance);
        this.interestRate = new BigDecimal(interestRate);
    }

    @Override
    public AccountType getType() {
        return AccountType.SAVINGS;
    }

    public BigDecimal getInterestRate() { return interestRate; }
}

