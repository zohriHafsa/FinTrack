# FinTrack360 Kafka Topic Naming & Event Schema

## Naming Convention

```
{domain}.{event}
```

**Rules:**
- All lowercase
- Dot-separated
- Domain first, event second
- Event name is past tense (something that happened)

---

## Topics Overview

| Topic | Producer | Consumer | Purpose |
|---|---|---|---|
| `company.registration` | auth-service | company-service | New SME registered, trigger company creation |
| `transaction.created` | transaction-service | budget-notification-service | New transaction recorded, trigger budget recalculation |
| `invoice.status.changed` | invoice-service | budget-notification-service | Invoice became overdue or was marked as paid |
| `budget.exceeded` | budget-notification-service | ai-insights-service | A budget limit was crossed |
| `notification.send` | budget-notification-service | — | Trigger in-app or email notification |

---

## Event Schemas

### `company.registration`
**Producer:** auth-service  
**Consumer:** company-service  
**Trigger:** A new business owner completes registration

```json
{
  "userId": "uuid",
  "email": "string",
  "companyName": "string",
  "sector": "string"
}
```

| Field | Type | Description |
|---|---|---|
| userId | UUID | The user ID created in auth-service DB |
| email | String | Owner's email address |
| companyName | String | Name of the SME |
| sector | String | Business sector (normalized, e.g. "Bakery") |

---

### `transaction.created`
**Producer:** transaction-service  
**Consumer:** budget-notification-service  
**Trigger:** A new income or expense transaction is recorded

```json
{
  "transactionId": "uuid",
  "companyId": "uuid",
  "categoryId": "uuid",
  "amount": "decimal",
  "type": "INCOME | EXPENSE",
  "date": "ISO-8601 date"
}
```

| Field | Type | Description |
|---|---|---|
| transactionId | UUID | Unique ID of the transaction |
| companyId | UUID | The company this transaction belongs to |
| categoryId | UUID | The category the transaction is assigned to |
| amount | Decimal | Transaction amount in MAD |
| type | Enum | INCOME or EXPENSE |
| date | String | Transaction date (e.g. "2026-07-31") |

---

### `invoice.status.changed`
**Producer:** invoice-service  
**Consumer:** budget-notification-service  
**Trigger:** An invoice becomes overdue (scheduled job) or is marked as paid

```json
{
  "invoiceId": "uuid",
  "companyId": "uuid",
  "clientId": "uuid",
  "status": "OVERDUE | PAID",
  "amount": "decimal",
  "dueDate": "ISO-8601 date"
}
```

| Field | Type | Description |
|---|---|---|
| invoiceId | UUID | Unique ID of the invoice |
| companyId | UUID | The company that issued the invoice |
| clientId | UUID | The external client who owes the payment |
| status | Enum | OVERDUE or PAID |
| amount | Decimal | Invoice amount in MAD |
| dueDate | String | Payment deadline (e.g. "2026-08-15") |

---

### `budget.exceeded`
**Producer:** budget-notification-service  
**Consumer:** ai-insights-service  
**Trigger:** A transaction pushes spend-to-date past the budget limit for a category

```json
{
  "budgetId": "uuid",
  "companyId": "uuid",
  "categoryId": "uuid",
  "budgetLimit": "decimal",
  "currentSpend": "decimal",
  "exceededAt": "ISO-8601 datetime"
}
```

| Field | Type | Description |
|---|---|---|
| budgetId | UUID | Unique ID of the budget |
| companyId | UUID | The company whose budget was exceeded |
| categoryId | UUID | The expense category that was exceeded |
| budgetLimit | Decimal | The configured spending limit |
| currentSpend | Decimal | Total spend after the triggering transaction |
| exceededAt | String | Timestamp of when the limit was crossed |

---

### `notification.send`
**Producer:** budget-notification-service  
**Consumer:** internal (notification storage + email sender)  
**Trigger:** A budget is exceeded or an invoice becomes overdue

```json
{
  "companyId": "uuid",
  "userId": "uuid",
  "type": "BUDGET_EXCEEDED | INVOICE_OVERDUE",
  "message": "string",
  "createdAt": "ISO-8601 datetime"
}
```

| Field | Type | Description |
|---|---|---|
| companyId | UUID | The company this notification belongs to |
| userId | UUID | The user who should receive the notification |
| type | Enum | BUDGET_EXCEEDED or INVOICE_OVERDUE |
| message | String | Human-readable notification message |
| createdAt | String | Timestamp the notification was generated |

---

## Application.yml Topic Config Reference

Add to each service's `application.yml` as needed:

```yaml
kafka:
  topics:
    company-registration: company.registration
    transaction-created: transaction.created
    invoice-status-changed: invoice.status.changed
    budget-exceeded: budget.exceeded
    notification-send: notification.send
```

---

[//]: # (## Notes)

[//]: # ()
[//]: # (- All timestamps use ISO-8601 format)

[//]: # (- All monetary amounts are in MAD &#40;Moroccan Dirham&#41;)

[//]: # (- All IDs are UUIDs &#40;v4&#41;)

[//]: # (- Event schemas are versioned implicitly — if a breaking change is needed, create a new topic &#40;e.g. `company.registration.v2`&#41;)

[//]: # (- `spring.json.add.type.headers: false` is set on all producers to keep events schema-agnostic)