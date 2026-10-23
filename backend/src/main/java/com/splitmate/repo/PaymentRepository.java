package com.splitmate.repo;

import com.splitmate.domain.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @EntityGraph(attributePaths = {"from", "to"})
    List<Payment> findByGroupIdOrderByIdDesc(Long groupId);
}
