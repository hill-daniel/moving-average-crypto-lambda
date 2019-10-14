package org.kiwi.calculation;

import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.BUY;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.NONE;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.SELL;

import java.math.BigDecimal;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState;

class AverageAlertState {

    AlertState evaluateStateWith(BigDecimal average, BigDecimal currencyValue, BigDecimal percentageThreshold) {
        BigDecimal deviation = calculateDeviation(average, currencyValue);
        if (isThresholdExceeded(deviation, percentageThreshold)) {
            return isValueHigherThanAverage(currencyValue, average) ? BUY : SELL;
        }
        return NONE;
    }

    private BigDecimal calculateDeviation(BigDecimal currentAverage, BigDecimal currentClosingQuote) {
        return new Deviation().calculate(currentAverage, currentClosingQuote);
    }

    private static boolean isThresholdExceeded(BigDecimal deviation, BigDecimal percentageThreshold) {
        return deviation.compareTo(percentageThreshold) >= 0;
    }

    private static boolean isValueHigherThanAverage(BigDecimal closingValue, BigDecimal lastAverage) {
        return closingValue.compareTo(lastAverage) == 1;
    }
}
