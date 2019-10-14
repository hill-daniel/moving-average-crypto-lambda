package org.kiwi.calculation;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.CEILING;

import java.math.BigDecimal;

class Deviation {

    private final static BigDecimal HUNDRED = new BigDecimal("100");

    BigDecimal calculate(BigDecimal currentAverage, BigDecimal currentClosingQuote) {
        if (currentClosingQuote == null || ZERO.equals(currentClosingQuote)) {
            return ZERO;
        }
        return calculateDeviationWith(currentAverage, currentClosingQuote);
    }

    private BigDecimal calculateDeviationWith(BigDecimal currentAverage, BigDecimal currentClosingQuote) {
        return currentAverage
                .multiply(HUNDRED)
                .divide(currentClosingQuote, CEILING)
                .subtract(HUNDRED)
                .abs();
    }
}