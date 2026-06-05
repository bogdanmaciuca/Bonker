package com.bonker.util;

import java.math.BigDecimal;

public class Validation {
    public static boolean isNonEmpty(String s) {
        return s != null && !s.isBlank();
    }

    public static boolean isValidCnp(String cnp) {
        return cnp != null && cnp.matches("\\d{13}");
    }

    public static boolean isValidIban(String iban) {
        return iban != null && iban.matches("RO\\d{2}[A-Z0-9]{18,22}");
    }

    public static boolean isValidCurrencyCode(String code) {
        return code != null && code.matches("[A-Z]{3}");
    }

    public static boolean isValidAccountType(String type) {
        return type != null && type.matches("(?i)checking|savings|fixed");
    }

    public static boolean isPositiveBigDecimal(String s) {
        try {
            return new BigDecimal(s).compareTo(BigDecimal.ZERO) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidRate(String s) {
        try {
            BigDecimal val = new BigDecimal(s);
            return val.compareTo(BigDecimal.ZERO) > 0 && val.compareTo(BigDecimal.ONE) <= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidCardNumber(String number) {
        return number != null && number.matches("\\d{4}-\\d{4}-\\d{4}-\\d{4}");
    }
}
