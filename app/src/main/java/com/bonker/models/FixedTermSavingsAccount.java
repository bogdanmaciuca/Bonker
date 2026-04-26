package com.bonker.models;

import java.math.BigDecimal;

public class FixedTermSavingsAccount extends Account {
    protected BigDecimal interestRate;

    public FixedTermSavingsAccount(String iban, Currency currency, BigDecimal balance) {
        super(iban, currency);
    }

    @Override
    public AccountType getType() {
        return AccountType.FIXED_TERM_SAVINGS;
    }
}

