# Backend Codebase Analysis - SpendSense

## 1. Executive Summary
**Project Type:** Personal Finance & Budget Tracker Backend API.
**Core Purpose:** Allows users to track multiple bank accounts, log income/expense transactions, set budgets, and view financial summaries.
**Tech Stack:** Java 21, Spring Boot 4.1.0-M1 (Bleeding Edge), PostgreSQL, Flyway, Clerk Auth (OAuth2), Bucket4j, Swagger.
**Status:** Functional but has critical concurrency issues, security gaps in production configuration, and missing implementation for recurring transactions.

## 2. Architecture & Design
**System Architecture:**
-   **Layered Monolith:** Standard Spring Boot Controller-Service-Repository architecture.
    -   **Controller:** Handles HTTP requests, validation, and mapping to/from DTOs.
    -   **Service:** Contains business logic (calculations, transaction management).
    -   **Repository:** Data access layer using Spring Data JPA.
    -   **Database:** PostgreSQL with relational schema.
-   **Security Integration:** External Identity Provider (Clerk) handles user management. Backend validates JWTs.
-   **Rate Limiting:** Implemented via Bucket4j as a custom filter.

**Scalability Considerations:**
-   **Stateless:** Uses JWT for authentication, making horizontal scaling possible.
-   **Database:** PostgreSQL is robust. Indexes are added for performance.
-   **Bottleneck:** Currently no caching layer (Redis) for frequent reads (e.g., user profile, dashboard stats).
-   **Concurrency:** Severe race condition in balance updates will cause data inconsistencies under load.

**Code Organization:**
-   **Package Structure:** Clean and standard (`controller`, `service`, `repository`, `model`, `dto`, `config`).
-   **DTOs:** Used effectively to decouple API from database entities.
-   **Mappers:** Manual mapping is used. It's verbose but clear.
-   **Lombok:** Extensively used to reduce boilerplate.

## 3. Database & Schema Analysis
**Tables:**
-   `users`: Stores user profile and Clerk ID.
-   `accounts`: Stores account details and current balance.
-   `transactions`: Stores individual transaction records. Linked to User and Account.
-   `budgets`: Simple 1-to-1 mapping with User. Stores total budget amount.

**Data Modeling:**
-   **Relationships:** Correctly modeled (One-to-Many, One-to-One).
-   **Data Types:** `BigDecimal` used for money (Correct). `UUID` for IDs (Good for distributed systems).
-   **Indexes:** `V8__add_performance_indexes.sql` adds crucial indexes on foreign keys and sorting columns (`date`).

**Normalization Issues:**
-   **Budget:** Very simplistic. Only one global budget amount per user. No category-wise budgets.
-   **Balance:** `Account.balance` is a calculated field stored in the database. This is a denormalization for performance but introduces risk of drift if transactions are modified without updating balance (which the code attempts to handle but has bugs).

## 4. Code Quality Review
**Strengths:**
-   **Readability:** Code is clean, well-named, and easy to follow.
-   **Modern Java:** Uses `var`, `switch` expressions, and Records (in some places or compatible).
-   **Tooling:** Flyway for migrations, Swagger for docs.

**Weaknesses:**
-   **Manual Mapping:** `TransactionMapper` manually sets fields. Easy to miss new fields.
-   **Hardcoded Values:** `OpenApiConfig` has `localhost:8000`. `RateLimitFilter` has hardcoded `/api/v1` logic.
-   **Test Coverage:** Tests use Mocks heavily. Integration tests with actual DB (TestContainers) are missing.

## 5. Bugs & Issues
### Critical: Concurrency Race Condition
In `TransactionService.updateAccountBalance`:
```java
BigDecimal currentBalance = account.getBalance();
BigDecimal newBalance = ...;
account.setBalance(newBalance);
accountRepository.save(account);
```
**Problem:** If two transactions occur simultaneously, both read the same `currentBalance`. The second write overwrites the first, leading to lost money.
**Fix:** Use `CASE WHEN` SQL update or `@Lock(PESSIMISTIC_WRITE)` on the Account entity.

### Major: Missing Recurring Transactions
-   `TransactionRepository` has `findByIsRecurringTrueAndNextRecurringDateBefore`.
-   **Problem:** This method is **never called**. There is no `@Scheduled` task or background job to process recurring transactions. Users will check "Recurring" but nothing will happen.

### Logic: Inefficient Updates
In `updateTransaction`:
1.  Reverts balance using *old* transaction state (Saved to DB).
2.  Updates transaction entity.
3.  Updates balance using *new* transaction state (Saved to DB).
**Problem:** This triggers multiple DB writes for the Account entity. It should be calculated in memory and saved once at the end of the transaction.

### Performance: N+1 Query Problem
-   `TransactionRepository.findByUserId` returns a Page of Transactions.
-   `TransactionMapper.toResponse` calls `transaction.getAccount().getName()`.
-   **Problem:** Since `Account` is `LAZY`, this triggers a separate SQL query for *each* transaction in the list.
-   **Fix:** Use `@EntityGraph(attributePaths = "account")` or `JOIN FETCH` in the repository query.

## 6. Security Evaluation
**Authentication:**
-   **Clerk Integration:** Good. Uses JWTs.
-   **Webhook Verification:** `WebhookVerificationService` uses Svix. This is secure and correct.

**Authorization:**
-   **Role Based:** `SecurityConfigDev` defines `ADMIN` and `USER` access.
-   **Gap:** The config class is named `SecurityConfigDev` and annotated with `@Profile("dev")`.
-   **Risk:** If deployed with `prod` profile, this bean is **skipped**. The application might default to "deny all" or "allow all" depending on Spring Boot defaults, or fail to start. **You must have a production security config.**

**Vulnerabilities:**
-   **Rate Limiting:** IP-based. If behind a load balancer (e.g. AWS ALB, Nginx), `request.getRemoteAddr()` might be the LB IP. The code correctly checks `X-Forwarded-For`, but this header can be spoofed if not sanitized by the edge proxy.

## 7. Production Readiness
**Missing Components:**
1.  **Production Security Configuration:** Essential.
2.  **Structured Logging:** Currently using `log.info` with string concatenation. Should use structured logging (JSON) for ELK/Splunk.
3.  **Metrics:** Spring Actuator is present but not configured for Prometheus/Grafana export.
4.  **Resilience:** No circuit breakers or retries for external calls (Clerk).
5.  **Environment Configuration:** `OpenApiConfig` has hardcoded localhost URL.

## 8. Portfolio/Resume Perspective
**Impression:**
-   **Solid Junior/Mid-Level Project:** It demonstrates ability to build a full CRUD API with authentication, database migrations, and documentation.
-   **Modern Stack:** Using Clerk and Flyway is a plus.

**How to Elevate:**
1.  **Fix Concurrency:** Implementing pessimistic locking or atomic updates shows deep understanding of database systems.
2.  **Implement Scheduler:** Use `@Scheduled` or Quartz to actually process recurring transactions.
3.  **Add Integration Tests:** Use `TestContainers` to test against a real PostgreSQL instance. This is a highly valued skill.
4.  **Dockerize:** Add a `Dockerfile` and `docker-compose.yml` for easy deployment.
5.  **CI/CD:** Add a GitHub Actions workflow to run tests and build.

---
**Verdict:** A good foundation but needs critical fixes (Concurrency, Security Config) before being considered "production grade".
