# SplitMate: group expense settlement

Split shared expenses with friends or roommates and see who owes whom.

**Stack:** Java 17, Spring Boot 3.3, Spring Data JPA, PostgreSQL 16, React 18 (Vite).

## Run it

```bash
# 1. Database
docker compose up -d

# 2. Backend  (http://localhost:8080)
cd backend
mvn spring-boot:run

# 3. Frontend (http://localhost:5173, proxies /api to the backend)
cd frontend
npm install
npm run dev
```

Run the tests with `cd backend && mvn test`. The calculator tests need no database.

Database settings can be overridden with `DB_URL`, `DB_USER`, `DB_PASSWORD`. Tables are created automatically (`ddl-auto: update`); switch to Flyway or Liquibase before production use.

## How the money logic works

All amounts are stored and calculated as **integer cents**, never floating point, so shares always add up to the total exactly.

| Split | Input per person | Rule |
|---|---|---|
| `EQUAL` | none | `total / n`. Leftover cents go to the lowest member ids, one each. |
| `PERCENTAGE` | percent | Must total exactly 100. Each share is floored to a cent, then leftover cents go to the largest fractional remainders (largest-remainder method). |
| `CUSTOM` | amount | Must be whole cents and add up to the expense total. |

**Balances.** For each person: `net = paid - owed`, adjusted by recorded repayments. Positive means the group owes them; negative means they owe the group. Balances always sum to zero.

**Settlement.** A greedy algorithm with two priority queues repeatedly matches the largest debtor to the largest creditor. It resolves n people in at most n-1 payments. The true minimum number of payments is NP-hard (it reduces to a subset-sum partition), and greedy is the standard practical answer.

**Recording repayments.** "Mark as paid" stores a payment, which shifts both people's balances, so the suggested settlement shrinks accordingly. Deleting an expense recomputes everything, because balances are always derived from history and never stored.

## API

| Method | Path | Purpose |
|---|---|---|
| GET/POST | `/api/groups` | List or create groups (`{name, members: [..]}`) |
| GET | `/api/groups/{id}` | Group with members |
| POST | `/api/groups/{id}/members` | Add a person (`{name}`) |
| GET/POST | `/api/groups/{id}/expenses` | History, or add an expense |
| DELETE | `/api/groups/{id}/expenses/{expenseId}` | Delete an expense |
| GET/POST | `/api/groups/{id}/payments` | Repayments, or record one (`{fromId, toId, amount}`) |
| GET | `/api/groups/{id}/settlement` | Net balances and suggested transfers |

Example expense, 120.00 split by percent:

```json
{
  "description": "Electricity",
  "amount": 120.00,
  "paidById": 1,
  "splitType": "PERCENTAGE",
  "date": "2026-10-23",
  "shares": [
    { "memberId": 1, "value": 50 },
    { "memberId": 2, "value": 30 },
    { "memberId": 3, "value": 20 }
  ]
}
```

For `EQUAL`, omit `value`. For `CUSTOM`, `value` is the amount in currency units.

## Layout

```
backend/src/main/java/com/splitmate/
  core/      pure logic: SplitCalculator, SettlementCalculator, Money (no Spring)
  domain/    JPA entities
  repo/      Spring Data repositories
  service/   SplitMateService (transactions, validation, DTO mapping)
  web/       REST controller, DTOs, error handling, CORS
frontend/src/
  components/  Sidebar, GroupView, Balances, ExpenseForm, History
```

## Ideas to extend

Currency per group, authentication and invites, recurring expenses, receipt uploads, and simplifying debts across multiple groups.
