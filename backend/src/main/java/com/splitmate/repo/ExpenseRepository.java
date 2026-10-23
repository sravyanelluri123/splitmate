package com.splitmate.repo;

import com.splitmate.domain.Expense;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @EntityGraph(attributePaths = {"paidBy", "shares", "shares.member"})
    List<Expense> findByGroupIdOrderByExpenseDateDescIdDesc(Long groupId);
}
