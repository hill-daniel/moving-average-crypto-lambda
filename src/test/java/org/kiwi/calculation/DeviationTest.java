package org.kiwi.calculation;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;

public class DeviationTest {

    private Deviation deviation;

    @Before
    public void setUp() throws Exception {
        deviation = new Deviation();
    }

    @Test
    public void should_calculate_deviation_of_positive_deviation() throws Exception {
        BigDecimal currentAverage = new BigDecimal("1000");
        BigDecimal currentClosingQuote = new BigDecimal("2000");

        BigDecimal deviation = this.deviation.calculate(currentAverage, currentClosingQuote);

        assertThat(deviation).isEqualTo(new BigDecimal("50"));
    }

    @Test
    public void should_calculate_deviation_of_negative_deviation() throws Exception {
        BigDecimal currentAverage = new BigDecimal("2000");
        BigDecimal currentClosingQuote = new BigDecimal("1000");

        BigDecimal deviation = this.deviation.calculate(currentAverage, currentClosingQuote);

        assertThat(deviation).isEqualTo(new BigDecimal("100"));
    }

    @Test
    public void should_return_zero_if_no_calculation_possible() throws Exception {
        BigDecimal deviation = this.deviation.calculate(new BigDecimal("1234"), BigDecimal.ZERO);

        assertThat(deviation).isEqualTo(BigDecimal.ZERO);
    }
}