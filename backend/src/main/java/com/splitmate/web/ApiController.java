package com.splitmate.web;

import com.splitmate.service.SplitMateService;
import com.splitmate.web.Dtos.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final SplitMateService service;

    public ApiController(SplitMateService service) { this.service = service; }

    @GetMapping("/groups")
    public List<GroupSummary> groups() { return service.listGroups(); }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupDto createGroup(@Valid @RequestBody CreateGroupRequest req) { return service.createGroup(req); }

    @GetMapping("/groups/{id}")
    public GroupDto group(@PathVariable Long id) { return service.getGroup(id); }

    @PostMapping("/groups/{id}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberDto addMember(@PathVariable Long id, @Valid @RequestBody AddMemberRequest req) {
        return service.addMember(id, req);
    }

    @GetMapping("/groups/{id}/expenses")
    public List<ExpenseDto> expenses(@PathVariable Long id) { return service.listExpenses(id); }

    @PostMapping("/groups/{id}/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseDto addExpense(@PathVariable Long id, @Valid @RequestBody CreateExpenseRequest req) {
        return service.addExpense(id, req);
    }

    @DeleteMapping("/groups/{id}/expenses/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable Long id, @PathVariable Long expenseId) {
        service.deleteExpense(id, expenseId);
    }

    @GetMapping("/groups/{id}/payments")
    public List<PaymentDto> payments(@PathVariable Long id) { return service.listPayments(id); }

    @PostMapping("/groups/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentDto recordPayment(@PathVariable Long id, @Valid @RequestBody CreatePaymentRequest req) {
        return service.recordPayment(id, req);
    }

    @GetMapping("/groups/{id}/settlement")
    public SettlementDto settlement(@PathVariable Long id) { return service.settlement(id); }
}
