package org.kiwi.calculation;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;

public class AverageTest {

    private Average average;

    @Before
    public void setUp() throws Exception {
        average = new Average();
    }

    @Test
    public void should_return_zero_for_empty_currencies() throws Exception {
        BigDecimal average = this.average.calculateFor(Collections.emptyList());

        assertThat(average).isEqualTo("0");
    }

    @Test
    public void should_calculate_average_from_list_of_currencies() throws Exception {
        Collection<BigDecimal> values = asList(
                new BigDecimal("10019.997793"),
                new BigDecimal("9845.234556"),
                new BigDecimal("7856.567886"),
                new BigDecimal("10119.567823"),
                new BigDecimal("10003.997793")
        );

        BigDecimal average = this.average.calculateFor(values);

        assertThat(average).isEqualTo(new BigDecimal("9569.073171"));
    }
}