package com.bonker.models;

import java.math.BigDecimal;

public class CheckingAccount extends Account {
    protected BigDecimal interestRate;

    public CheckingAccount(String iban, Currency currency, BigDecimal balance) {
        super(iban, currency);
    }

    public CheckingAccount(String iban, Currency currency, double balance) {
        super(iban, currency);
    }

    @Override
    public AccountType getType() {
        return AccountType.CHECKING;
    }
}


