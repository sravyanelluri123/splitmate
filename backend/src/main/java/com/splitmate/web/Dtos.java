package com.splitmate.web;

import com.splitmate.core.SplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/** Request and response shapes. Money is exchanged as decimals (12.34) and stored as cents. */
public final class Dtos {
    private Dtos() {}

    // ---- requests ----
    public record CreateGroupRequest(@NotBlank String name, List<@NotBlank String> members) {}

    public record AddMemberRequest(@NotBlank String name) {}

    /** value: ignored for EQUAL, percent for PERCENTAGE, amount for CUSTOM. */
    public record ShareRequest(@NotNull Long memberId, BigDecimal value) {}

    public record CreateExpenseRequest(
            @NotBlank String description,
            @NotNull @Positive BigDecimal amount,
            @NotNull Long paidById,
            @NotNull SplitType splitType,
            LocalDate date,
            @NotEmpty @Valid List<ShareRequest> shares) {}

    public record CreatePaymentRequest(
            @NotNull Long fromId,
            @NotNull Long toId,
            @NotNull @Positive BigDecimal amount) {}

    // ---- responses ----
    public record MemberDto(Long id, String name) {}

    public record GroupSummary(Long id, String name, int memberCount) {}

    public record GroupDto(Long id, String name, List<MemberDto> members) {}

    public record ShareDto(Long memberId, String memberName, BigDecimal amount) {}

    public record ExpenseDto(Long id, String description, BigDecimal amount, Long paidById, String paidByName,
                             SplitType splitType, LocalDate date, Instant createdAt, List<ShareDto> shares) {}

    public record PaymentDto(Long id, Long fromId, String fromName, Long toId, String toName,
                             BigDecimal amount, Instant createdAt) {}

    /** net > 0: the group owes this person. net < 0: this person owes the group. */
    public record BalanceDto(Long memberId, String name, BigDecimal net) {}

    public record TransferDto(Long fromId, String fromName, Long toId, String toName, BigDecimal amount) {}

    public record SettlementDto(List<BalanceDto> balances, List<TransferDto> transfers) {}
}
