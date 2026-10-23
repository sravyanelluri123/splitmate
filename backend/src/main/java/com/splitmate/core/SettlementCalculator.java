package com.splitmate.core;

import java.util.*;

/**
 * Works out who should pay whom to bring everyone to zero.
 *
 * Net balance convention: positive = the group owes this person, negative = this person owes the group.
 *
 * Greedy (largest debtor pays largest creditor) settles n people in at most n-1 payments.
 * Finding the true minimum is NP-hard (a subset-sum style partition problem),
 * and greedy is the standard practical answer.
 */
public final class SettlementCalculator {

    public record Transfer(long fromId, long toId, long cents) {}

    public record ExpenseLine(long paidById, long totalCents, Map<Long, Long> shares) {}

    private SettlementCalculator() {}

    /** Net balance per member: what they paid minus what they owe, adjusted by recorded repayments. */
    public static Map<Long, Long> netBalances(Collection<Long> memberIds,
                                              List<ExpenseLine> expenses,
                                              List<Transfer> recordedPayments) {
        Map<Long, Long> net = new LinkedHashMap<>();
        for (Long id : memberIds) net.put(id, 0L);
        for (ExpenseLine e : expenses) {
            net.merge(e.paidById(), e.totalCents(), Long::sum);
            for (Map.Entry<Long, Long> s : e.shares().entrySet()) net.merge(s.getKey(), -s.getValue(), Long::sum);
        }
        // Someone who paid back a debt owes less; the recipient is owed less.
        for (Transfer p : recordedPayments) {
            net.merge(p.fromId(), p.cents(), Long::sum);
            net.merge(p.toId(), -p.cents(), Long::sum);
        }
        return net;
    }

    public static List<Transfer> settle(Map<Long, Long> net) {
        long check = net.values().stream().mapToLong(Long::longValue).sum();
        if (check != 0) throw new IllegalStateException("Balances do not sum to zero (" + check + " cents off).");

        // Max-heaps by amount; ties broken by id so output is stable.
        Comparator<long[]> byAmountDesc = (a, b) -> a[1] != b[1] ? Long.compare(b[1], a[1]) : Long.compare(a[0], b[0]);
        PriorityQueue<long[]> creditors = new PriorityQueue<>(byAmountDesc);
        PriorityQueue<long[]> debtors = new PriorityQueue<>(byAmountDesc);
        for (Map.Entry<Long, Long> e : net.entrySet()) {
            if (e.getValue() > 0) creditors.add(new long[]{e.getKey(), e.getValue()});
            else if (e.getValue() < 0) debtors.add(new long[]{e.getKey(), -e.getValue()});
        }

        List<Transfer> out = new ArrayList<>();
        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            long[] c = creditors.poll();
            long[] d = debtors.poll();
            long pay = Math.min(c[1], d[1]);
            out.add(new Transfer(d[0], c[0], pay));
            c[1] -= pay;
            d[1] -= pay;
            if (c[1] > 0) creditors.add(c);
            if (d[1] > 0) debtors.add(d);
        }
        return out;
    }
}
