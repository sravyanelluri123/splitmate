package com.splitmate.core;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {
    private Money() {}

    /** Converts a decimal amount (e.g. 12.34) to cents; rejects fractions of a cent. */
    public static long toCents(BigDecimal amount) {
        try {
            return amount.movePointRight(2).setScale(0, RoundingMode.UNNECESSARY).longValueExact();
        } catch (ArithmeticException e) {
            throw new InvalidSplitException("Amounts can have at most two decimal places.");
        }
    }

    public static BigDecimal fromCents(long cents) {
        return BigDecimal.valueOf(cents, 2);
    }

    public static String format(long cents) {
        return fromCents(cents).toPlainString();
    }
}
