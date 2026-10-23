package com.splitmate.service;

import com.splitmate.core.*;
import com.splitmate.core.SettlementCalculator.ExpenseLine;
import com.splitmate.core.SettlementCalculator.Transfer;
import com.splitmate.core.SplitCalculator.ShareInput;
import com.splitmate.domain.*;
import com.splitmate.repo.ExpenseRepository;
import com.splitmate.repo.GroupRepository;
import com.splitmate.repo.PaymentRepository;
import com.splitmate.web.ApiException;
import com.splitmate.web.Dtos.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class SplitMateService {

    private final GroupRepository groups;
    private final ExpenseRepository expenses;
    private final PaymentRepository payments;

    public SplitMateService(GroupRepository groups, ExpenseRepository expenses, PaymentRepository payments) {
        this.groups = groups;
        this.expenses = expenses;
        this.payments = payments;
    }

    // ---------- groups & members ----------

    public GroupDto createGroup(CreateGroupRequest req) {
        ExpenseGroup group = new ExpenseGroup(req.name().trim());
        if (req.members() != null) req.members().forEach(n -> group.addMember(n.trim()));
        return toDto(groups.save(group));
    }

    @Transactional(readOnly = true)
    public List<GroupSummary> listGroups() {
        return groups.findAllByOrderByCreatedAtDesc().stream()
                .map(g -> new GroupSummary(g.getId(), g.getName(), g.getMembers().size()))
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupDto getGroup(Long id) { return toDto(findGroup(id)); }

    public MemberDto addMember(Long groupId, AddMemberRequest req) {
        ExpenseGroup group = findGroup(groupId);
        group.addMember(req.name().trim());
        groups.saveAndFlush(group);
        List<Member> members = group.getMembers();
        Member added = members.get(members.size() - 1);
        return new MemberDto(added.getId(), added.getName());
    }

    // ---------- expenses ----------

    public ExpenseDto addExpense(Long groupId, CreateExpenseRequest req) {
        ExpenseGroup group = findGroup(groupId);
        Map<Long, Member> byId = membersById(group);

        Member payer = byId.get(req.paidById());
        if (payer == null) throw ApiException.badRequest("The payer is not in this group.");
        for (ShareRequest s : req.shares()) {
            if (!byId.containsKey(s.memberId())) throw ApiException.badRequest("Someone in the split is not in this group.");
        }

        long total = Money.toCents(req.amount());
        List<ShareInput> inputs = req.shares().stream()
                .map(s -> new ShareInput(s.memberId(), s.value()))
                .toList();
        Map<Long, Long> resolved = SplitCalculator.calculate(req.splitType(), total, inputs);

        Expense expense = new Expense(group, req.description().trim(), total, payer, req.splitType(),
                req.date() != null ? req.date() : LocalDate.now());
        resolved.forEach((memberId, cents) -> expense.addShare(byId.get(memberId), cents));
        return toDto(expenses.save(expense));
    }

    @Transactional(readOnly = true)
    public List<ExpenseDto> listExpenses(Long groupId) {
        findGroup(groupId);
        return expenses.findByGroupIdOrderByExpenseDateDescIdDesc(groupId).stream().map(this::toDto).toList();
    }

    public void deleteExpense(Long groupId, Long expenseId) {
        Expense e = expenses.findById(expenseId)
                .filter(x -> x.getGroup().getId().equals(groupId))
                .orElseThrow(() -> ApiException.notFound("Expense"));
        expenses.delete(e);
    }

    // ---------- payments ----------

    public PaymentDto recordPayment(Long groupId, CreatePaymentRequest req) {
        ExpenseGroup group = findGroup(groupId);
        Map<Long, Member> byId = membersById(group);
        Member from = byId.get(req.fromId());
        Member to = byId.get(req.toId());
        if (from == null || to == null) throw ApiException.badRequest("Both people must be in this group.");
        if (from.getId().equals(to.getId())) throw ApiException.badRequest("Someone cannot pay themselves.");
        return toDto(payments.save(new Payment(group, from, to, Money.toCents(req.amount()))));
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> listPayments(Long groupId) {
        findGroup(groupId);
        return payments.findByGroupIdOrderByIdDesc(groupId).stream().map(this::toDto).toList();
    }

    // ---------- settlement ----------

    @Transactional(readOnly = true)
    public SettlementDto settlement(Long groupId) {
        ExpenseGroup group = findGroup(groupId);
        Map<Long, Member> byId = membersById(group);

        List<ExpenseLine> lines = expenses.findByGroupIdOrderByExpenseDateDescIdDesc(groupId).stream()
                .map(e -> new ExpenseLine(
                        e.getPaidBy().getId(),
                        e.getAmountCents(),
                        e.getShares().stream().collect(Collectors.toMap(
                                s -> s.getMember().getId(), ExpenseShare::getShareCents, Long::sum))))
                .toList();
        List<Transfer> repaid = payments.findByGroupIdOrderByIdDesc(groupId).stream()
                .map(p -> new Transfer(p.getFrom().getId(), p.getTo().getId(), p.getAmountCents()))
                .toList();

        Map<Long, Long> net = SettlementCalculator.netBalances(byId.keySet(), lines, repaid);

        List<BalanceDto> balances = net.entrySet().stream()
                .map(e -> new BalanceDto(e.getKey(), byId.get(e.getKey()).getName(), Money.fromCents(e.getValue())))
                .toList();
        List<TransferDto> transfers = SettlementCalculator.settle(net).stream()
                .map(t -> new TransferDto(t.fromId(), byId.get(t.fromId()).getName(),
                        t.toId(), byId.get(t.toId()).getName(), Money.fromCents(t.cents())))
                .toList();
        return new SettlementDto(balances, transfers);
    }

    // ---------- helpers ----------

    private ExpenseGroup findGroup(Long id) {
        return groups.findById(id).orElseThrow(() -> ApiException.notFound("Group"));
    }

    private Map<Long, Member> membersById(ExpenseGroup g) {
        return g.getMembers().stream().collect(Collectors.toMap(
                Member::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
    }

    private GroupDto toDto(ExpenseGroup g) {
        return new GroupDto(g.getId(), g.getName(),
                g.getMembers().stream().map(m -> new MemberDto(m.getId(), m.getName())).toList());
    }

    private ExpenseDto toDto(Expense e) {
        return new ExpenseDto(e.getId(), e.getDescription(), Money.fromCents(e.getAmountCents()),
                e.getPaidBy().getId(), e.getPaidBy().getName(), e.getSplitType(), e.getExpenseDate(), e.getCreatedAt(),
                e.getShares().stream()
                        .map(s -> new ShareDto(s.getMember().getId(), s.getMember().getName(), Money.fromCents(s.getShareCents())))
                        .toList());
    }

    private PaymentDto toDto(Payment p) {
        return new PaymentDto(p.getId(), p.getFrom().getId(), p.getFrom().getName(),
                p.getTo().getId(), p.getTo().getName(), Money.fromCents(p.getAmountCents()), p.getCreatedAt());
    }
}
