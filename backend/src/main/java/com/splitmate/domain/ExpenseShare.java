package com.splitmate.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "expense_shares")
public class ExpenseShare {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id")
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(nullable = false)
    private long shareCents;

    protected ExpenseShare() {}
    ExpenseShare(Expense expense, Member member, long shareCents) {
        this.expense = expense;
        this.member = member;
        this.shareCents = shareCents;
    }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public long getShareCents() { return shareCents; }
}
