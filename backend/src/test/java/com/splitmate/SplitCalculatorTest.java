package com.splitmate;

import com.splitmate.core.*;
import com.splitmate.core.SplitCalculator.ShareInput;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SplitCalculatorTest {

    private static ShareInput in(long id, String v) { return new ShareInput(id, v == null ? null : new BigDecimal(v)); }

    @Test
    void equalSplitGivesLeftoverCentsToLowestIds() {
        Map<Long, Long> r = SplitCalculator.calculate(SplitType.EQUAL, 10000, List.of(in(3, null), in(1, null), in(2, null)));
        assertEquals(Map.of(1L, 3334L, 2L, 3333L, 3L, 3333L), r);
    }

    @Test
    void percentageSplitAlwaysSumsToTotal() {
        Map<Long, Long> r = SplitCalculator.calculate(SplitType.PERCENTAGE, 100,
                List.of(in(1, "33.333"), in(2, "33.333"), in(3, "33.334")));
        assertEquals(100L, r.values().stream().mapToLong(Long::longValue).sum());
    }

    @Test
    void percentageMustTotalOneHundred() {
        assertThrows(InvalidSplitException.class, () ->
                SplitCalculator.calculate(SplitType.PERCENTAGE, 100, List.of(in(1, "60"), in(2, "30"))));
    }

    @Test
    void customAmountsMustMatchTotal() {
        assertEquals(Map.of(1L, 2050L, 2L, 2950L),
                SplitCalculator.calculate(SplitType.CUSTOM, 5000, List.of(in(1, "20.50"), in(2, "29.50"))));
        assertThrows(InvalidSplitException.class, () ->
                SplitCalculator.calculate(SplitType.CUSTOM, 5000, List.of(in(1, "20"), in(2, "29"))));
    }

    @Test
    void rejectsSubCentAmountsAndDuplicates() {
        assertThrows(InvalidSplitException.class, () ->
                SplitCalculator.calculate(SplitType.CUSTOM, 5000, List.of(in(1, "20.005"), in(2, "29.995"))));
        assertThrows(InvalidSplitException.class, () ->
                SplitCalculator.calculate(SplitType.EQUAL, 5000, List.of(in(1, null), in(1, null))));
    }
}
