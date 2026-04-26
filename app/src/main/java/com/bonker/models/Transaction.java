package com.bonker.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record Transaction(TransactionType type, BigDecimal amount, Currency currency, LocalDateTime timestamp) {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm");

    public Transaction(TransactionType type, BigDecimal amount, Currency currency) {
        this(type, amount, currency, LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "Transaction[" + type + " of " + amount + currency + " at " + timestamp.format(TIME_FORMATTER) + "]";
    }
}

