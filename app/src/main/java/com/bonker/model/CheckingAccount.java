package com.bonker.model;

import java.math.BigDecimal;

public class CheckingAccount extends Account {
    public CheckingAccount(String iban, Currency currency, BigDecimal balance) {
        super(iban, currency, balance);
    }

    public CheckingAccount(String iban, Currency currency, double balance) {
        super(iban, currency, balance);
    }

    @Override
    public AccountType getType() {
        return AccountType.CHECKING;
    }
}


