package com.splitmate;

import com.splitmate.core.SettlementCalculator;
import com.splitmate.core.SettlementCalculator.ExpenseLine;
import com.splitmate.core.SettlementCalculator.Transfer;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SettlementCalculatorTest {

    @Test
    void settlesSimpleGroup() {
        // Ann(1) paid 90 for everyone; Bo(2) paid 30 for Bo and Cy(3).
        var lines = List.of(
                new ExpenseLine(1, 9000, Map.of(1L, 3000L, 2L, 3000L, 3L, 3000L)),
                new ExpenseLine(2, 3000, Map.of(2L, 1500L, 3L, 1500L)));
        var net = SettlementCalculator.netBalances(List.of(1L, 2L, 3L), lines, List.of());
        assertEquals(Map.of(1L, 6000L, 2L, -1500L, 3L, -4500L), net);
        assertEquals(List.of(new Transfer(3, 1, 4500), new Transfer(2, 1, 1500)), SettlementCalculator.settle(net));
    }

    @Test
    void recordedPaymentsReduceWhatIsOwed() {
        var lines = List.of(new ExpenseLine(1, 9000, Map.of(1L, 3000L, 2L, 3000L, 3L, 3000L)));
        var net = SettlementCalculator.netBalances(List.of(1L, 2L, 3L), lines, List.of(new Transfer(3, 1, 3000)));
        assertEquals(List.of(new Transfer(2, 1, 3000)), SettlementCalculator.settle(net));
    }

    @Test
    void randomBalancesAlwaysZeroOutWithAtMostNMinusOneTransfers() {
        Random r = new Random(42);
        for (int t = 0; t < 500; t++) {
            int n = 2 + r.nextInt(8);
            Map<Long, Long> bal = new LinkedHashMap<>();
            long sum = 0;
            for (long i = 1; i < n; i++) { long v = r.nextInt(20001) - 10000; bal.put(i, v); sum += v; }
            bal.put((long) n, -sum);

            var transfers = SettlementCalculator.settle(bal);
            Map<Long, Long> after = new HashMap<>(bal);
            for (var tr : transfers) {
                after.merge(tr.fromId(), tr.cents(), Long::sum);
                after.merge(tr.toId(), -tr.cents(), Long::sum);
            }
            assertTrue(after.values().stream().allMatch(v -> v == 0));
            assertTrue(transfers.size() <= n - 1);
        }
    }
}
