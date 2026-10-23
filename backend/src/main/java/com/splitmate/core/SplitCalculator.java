package com.splitmate.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Turns "who takes part and how" into exact per-person cent amounts.
 *
 * All money is handled as long cents so shares always add up to the total
 * with no floating-point drift. Leftover cents are handed out deterministically,
 * so the same input always yields the same split.
 */
public final class SplitCalculator {

    /** One participant. value is ignored for EQUAL, a percent for PERCENTAGE, an amount for CUSTOM. */
    public record ShareInput(long memberId, BigDecimal value) {}

    private SplitCalculator() {}

    public static Map<Long, Long> calculate(SplitType type, long totalCents, List<ShareInput> inputs) {
        if (totalCents <= 0) throw new InvalidSplitException("Amount must be greater than zero.");
        if (inputs == null || inputs.isEmpty()) throw new InvalidSplitException("Pick at least one person to split with.");
        Set<Long> seen = new HashSet<>();
        for (ShareInput in : inputs) {
            if (!seen.add(in.memberId())) throw new InvalidSplitException("A person appears more than once in the split.");
        }
        return switch (type) {
            case EQUAL -> equal(totalCents, inputs);
            case PERCENTAGE -> percentage(totalCents, inputs);
            case CUSTOM -> custom(totalCents, inputs);
        };
    }

    /** Everyone pays total/n; the first (total mod n) people, in id order, pay one extra cent. */
    static Map<Long, Long> equal(long total, List<ShareInput> inputs) {
        List<Long> ids = inputs.stream().map(ShareInput::memberId).sorted().toList();
        int n = ids.size();
        long base = total / n;
        int extra = (int) (total % n);
        Map<Long, Long> out = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) out.put(ids.get(i), base + (i < extra ? 1 : 0));
        return out;
    }

    /**
     * Percentages must total 100. Each share is floored to a cent, then the cents
     * left over go to the largest fractional remainders (largest-remainder method).
     */
    static Map<Long, Long> percentage(long total, List<ShareInput> inputs) {
        BigDecimal sum = BigDecimal.ZERO;
        for (ShareInput in : inputs) {
            requireValue(in);
            if (in.value().signum() < 0) throw new InvalidSplitException("Percentages cannot be negative.");
            sum = sum.add(in.value());
        }
        if (sum.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new InvalidSplitException("Percentages must add up to 100 (currently "
                    + sum.stripTrailingZeros().toPlainString() + ").");
        }

        record Row(long id, long floor, BigDecimal frac) {}
        List<Row> rows = new ArrayList<>();
        long allocated = 0;
        for (ShareInput in : inputs) {
            BigDecimal exact = BigDecimal.valueOf(total).multiply(in.value()).divide(BigDecimal.valueOf(100));
            BigDecimal floor = exact.setScale(0, RoundingMode.FLOOR);
            rows.add(new Row(in.memberId(), floor.longValueExact(), exact.subtract(floor)));
            allocated += floor.longValueExact();
        }
        long leftover = total - allocated;
        List<Row> ranked = new ArrayList<>(rows);
        ranked.sort(Comparator.comparing(Row::frac).reversed().thenComparingLong(Row::id));
        Set<Long> bump = new HashSet<>();
        for (int i = 0; i < leftover; i++) bump.add(ranked.get(i).id());

        Map<Long, Long> out = new LinkedHashMap<>();
        for (Row r : rows) out.put(r.id(), r.floor() + (bump.contains(r.id()) ? 1 : 0));
        return out;
    }

    /** Exact amounts: must be whole cents and add up to the total. */
    static Map<Long, Long> custom(long total, List<ShareInput> inputs) {
        Map<Long, Long> out = new LinkedHashMap<>();
        long sum = 0;
        for (ShareInput in : inputs) {
            requireValue(in);
            if (in.value().signum() < 0) throw new InvalidSplitException("Amounts cannot be negative.");
            long cents = Money.toCents(in.value());
            out.put(in.memberId(), cents);
            sum += cents;
        }
        if (sum != total) {
            throw new InvalidSplitException("Custom amounts add up to " + Money.format(sum)
                    + " but the expense is " + Money.format(total) + ".");
        }
        return out;
    }

    private static void requireValue(ShareInput in) {
        if (in.value() == null) throw new InvalidSplitException("Every person in the split needs a value.");
    }
}
