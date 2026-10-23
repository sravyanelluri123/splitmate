package com.splitmate.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expense_groups")
public class ExpenseGroup {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Member> members = new ArrayList<>();

    protected ExpenseGroup() {}
    public ExpenseGroup(String name) { this.name = name; }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
    public List<Member> getMembers() { return members; }

    public Member addMember(String memberName) {
        Member m = new Member(this, memberName);
        members.add(m);
        return m;
    }
}
