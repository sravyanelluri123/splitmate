package com.splitmate.domain;

import com.splitmate.core.SplitType;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
public class Expense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id")
    private ExpenseGroup group;

    @Column(nullable = false)
    private String description;

    /** Stored in cents to avoid floating-point error. */
    @Column(nullable = false)
    private long amountCents;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paid_by_id")
    private Member paidBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SplitType splitType;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseShare> shares = new ArrayList<>();

    protected Expense() {}

    public Expense(ExpenseGroup group, String description, long amountCents, Member paidBy,
                   SplitType splitType, LocalDate expenseDate) {
        this.group = group;
        this.description = description;
        this.amountCents = amountCents;
        this.paidBy = paidBy;
        this.splitType = splitType;
        this.expenseDate = expenseDate;
    }

    public void addShare(Member member, long shareCents) {
        shares.add(new ExpenseShare(this, member, shareCents));
    }

    public Long getId() { return id; }
    public ExpenseGroup getGroup() { return group; }
    public String getDescription() { return description; }
    public long getAmountCents() { return amountCents; }
    public Member getPaidBy() { return paidBy; }
    public SplitType getSplitType() { return splitType; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public Instant getCreatedAt() { return createdAt; }
    public List<ExpenseShare> getShares() { return shares; }
}
