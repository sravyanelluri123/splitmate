package com.splitmate.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "members")
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id")
    private ExpenseGroup group;

    protected Member() {}
    Member(ExpenseGroup group, String name) { this.group = group; this.name = name; }

    public Long getId() { return id; }
    public String getName() { return name; }
    public ExpenseGroup getGroup() { return group; }
}
