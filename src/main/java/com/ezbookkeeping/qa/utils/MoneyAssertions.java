package com.ezbookkeeping.qa.utils;

import java.math.BigDecimal;

import static com.ezbookkeeping.qa.utils.MoneyUtils.normalize;
import static com.ezbookkeeping.qa.utils.MoneyUtils.parse;
import static org.assertj.core.api.Assertions.assertThat;

public final class MoneyAssertions {

    private MoneyAssertions() {
    }

    public static void assertMoneyEquals(BigDecimal actual, BigDecimal expected) {
        assertThat(normalize(actual)).isEqualByComparingTo(normalize(expected));
    }

    public static void assertMoneyEquals(BigDecimal actual, String expected) {
        assertMoneyEquals(actual, parse(expected));
    }

    public static void assertCentsEquals(long actualCents, long expectedCents) {
        assertThat(actualCents).isEqualTo(expectedCents);
    }

    public static void assertBalanceAfterExpense(String initial, String expense, String expectedBalance) {
        BigDecimal actual = parse(initial).subtract(parse(expense));
        assertMoneyEquals(actual, parse(expectedBalance));
    }

    public static void assertBalanceAfterTransfer(
            String sourceInitial, String destInitial,
            String transferAmount,
            String expectedSource, String expectedDest) {

        BigDecimal srcInit = parse(sourceInitial);
        BigDecimal dstInit = parse(destInitial);
        BigDecimal amount = parse(transferAmount);

        assertMoneyEquals(srcInit.subtract(amount), parse(expectedSource));
        assertMoneyEquals(dstInit.add(amount), parse(expectedDest));
    }
}