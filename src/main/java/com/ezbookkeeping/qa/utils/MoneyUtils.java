package com.ezbookkeeping.qa.utils;

import java.math.BigDecimal;

public final class MoneyUtils {

    private static final int SCALE = 2;

    private MoneyUtils() {
    }

    public static long toCents(BigDecimal value) {
        return value.movePointRight(2).longValueExact();
    }

    public static long toCents(double value) {
        return toCents(BigDecimal.valueOf(value));
    }

    public static BigDecimal fromCents(long cents) {
        return BigDecimal.valueOf(cents, 2);
    }

    public static BigDecimal parse(String value) {
        return new BigDecimal(value);
    }

    public static BigDecimal normalize(BigDecimal value) {
        return value.setScale(SCALE, java.math.RoundingMode.HALF_UP);
    }
}