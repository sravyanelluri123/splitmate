package com.splitmate.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** A real-world repayment between two members, recorded so balances reflect it. */
@Entity
@Table(name = "payments")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id")
    private ExpenseGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_id")
    private Member from;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_id")
    private Member to;

    @Column(nullable = false)
    private long amountCents;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Payment() {}
    public Payment(ExpenseGroup group, Member from, Member to, long amountCents) {
        this.group = group;
        this.from = from;
        this.to = to;
        this.amountCents = amountCents;
    }

    public Long getId() { return id; }
    public ExpenseGroup getGroup() { return group; }
    public Member getFrom() { return from; }
    public Member getTo() { return to; }
    public long getAmountCents() { return amountCents; }
    public Instant getCreatedAt() { return createdAt; }
}
