package com.ezbookkeeping.qa.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

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

    public static void assertMoneyEquals(BigDecimal actual, BigDecimal expected) {
        assertThat(actual.setScale(SCALE, RoundingMode.HALF_UP))
                .isEqualByComparingTo(expected.setScale(SCALE, RoundingMode.HALF_UP));
    }

    public static void assertMoneyEquals(BigDecimal actual, String expected) {
        assertMoneyEquals(actual, new BigDecimal(expected));
    }

    public static void assertCentsEquals(long actualCents, long expectedCents) {
        assertThat(actualCents).isEqualTo(expectedCents);
    }

    public static void assertBalanceAfterExpense(String initial, String expense, String expectedBalance) {
        BigDecimal init = parse(initial);
        BigDecimal exp = parse(expense);
        BigDecimal expected = parse(expectedBalance);
        BigDecimal actual = init.subtract(exp);
        assertMoneyEquals(actual, expected);
    }

    public static void assertBalanceAfterTransfer(
            String sourceInitial, String destInitial,
            String transferAmount,
            String expectedSource, String expectedDest) {

        BigDecimal srcInit = parse(sourceInitial);
        BigDecimal dstInit = parse(destInitial);
        BigDecimal amount = parse(transferAmount);

        assertMoneyEquals(srcInit.subtract(amount), expectedSource);
        assertMoneyEquals(dstInit.add(amount), expectedDest);
    }
}
