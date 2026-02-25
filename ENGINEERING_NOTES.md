# Engineering Notes - Campaign Service (Backend Part 3)

These notes describe a **simplified backend implementation** of the Campaign Service API
for the take‑home assessment. My goal was to implement the core flows cleanly in the
time available, rather than building every advanced feature in production depth.

---

## 1. Architecture decisions and trade‑offs

- **Simple layered Spring Boot app**
  - Controllers: expose the required Campaign APIs (`POST /campaigns`, `GET /campaigns`,
    `GET /campaigns/{id}`, `POST /campaigns/{id}/retry-failures`).
  - Services: contain the main business logic (validate tenant, create campaign,
    parse CSV, basic stats).
  - Repositories: use Spring Data JPA for `Tenant`, `Campaign`, and `Recipient`.

- **Multi‑tenancy (logical only)**
  - Each row has a `tenant_id` column (`Tenant`, `Campaign`, `Recipient`).
  - Queries are always scoped by `tenantId` (e.g. `findByTenantId`, `findByIdAndTenantId`).
  - I did **not** implement separate schemas or databases per tenant; this version focuses
    on the shared‑schema approach that is common in smaller SaaS apps.

- **Async and background work (simplified)**
  - The assessment asks for fully async processing with worker pools / queues.
  - In this version, campaign creation:
    - Validates the tenant.
    - Saves the `Campaign`.
    - Parses and stores recipients from CSV in batches using `CsvParsingService`.
  - I **did not** wire a real queue, transactional outbox, or a full worker pool.
    Those are called out explicitly as future improvements below.

- **Notification sending**
  - I focused on the data model (`Campaign`, `Recipient`) and CSV ingestion.
  - A real implementation would have a `NotificationJob` table and a worker that
    calls simulated provider endpoints; this version keeps that at the design level only.

Trade‑off: I chose a straightforward, understandable Spring Boot design over a more
advanced event‑driven or microservice architecture to keep the code appropriate for
my experience level and the time box.

---

## 2. How the system scales (in this version)

- **Vertical scaling**
  - The app is a single Spring Boot service that can be run on a larger instance
    (more CPU/memory) if needed.

- **Database‑friendly CSV processing**
  - `CsvParsingService` reads the CSV using a `BufferedReader` and Apache Commons CSV.
  - Recipients are saved in **batches** (configurable batch size), so the API does not
    try to insert everything one row at a time.

- **Indexes and simple queries**
  - The repositories use straightforward queries such as `findByTenantId` and
    `countByCampaignId`.
  - In a real deployment, I would add indexes on `tenant_id`, `campaign_id`, and
    `status` columns; I left detailed indexing as future work.

- **How I would extend scaling later**
  - Introduce a proper **job/queue** layer (e.g. a `NotificationJob` table plus a worker
    that polls pending jobs).
  - Add **rate limiting** and **back‑pressure** around provider calls (e.g. Token Bucket).
  - Consider splitting “Campaign Management” and “Notification Delivery” into
    separate modules or services if traffic grows.

---

## 3. Failure scenarios considered

- **Invalid tenant**
  - If a `tenantId` does not exist, `CampaignService.createCampaign` throws a
    `TenantNotFoundException`, which is handled by a global exception handler and
    returned as a clean API error.

- **CSV parsing errors**
  - If the CSV is missing required headers (like `recipientId`) or has invalid rows,
    `CsvParsingService` throws a `CsvParseException` with a readable message.
  - The controller surfaces this as an error response instead of crashing the app.

- **Empty or missing CSV**
  - If the file is empty or not provided, the service logs a warning and proceeds
    with `0` recipients.

- **Basic retry endpoint**
  - `POST /campaigns/{id}/retry-failures` exists, but in this simplified version it
    only validates that the campaign belongs to the tenant and returns `0`. A real
    retry mechanism is listed under “Known limitations”.

I did **not** fully implement provider failures, exponential backoff, or circuit
breakers in this iteration.

---

## 4. Known limitations (intentional for this take‑home)

These are limitations I am aware of and would address with more time:

- **No real async worker / queue**
  - The current implementation does not have a background worker reading from a
    queue or `NotificationJob` table.
  - The design could be extended to use:
    - A job table with `PENDING/PROCESSING/SENT/FAILED` states, or
    - An external queue (e.g. Kafka/RabbitMQ) plus a worker service.

- **No strong idempotency guarantees**
  - There is no idempotency key or unique constraint to guarantee that a recipient
    never receives the same notification twice after retries/crashes.

- **Rate limiting is not implemented**
  - The code does not enforce the “100 requests/min per channel” requirement yet.
  - A future version could use a token bucket algorithm or a library like Bucket4j.

- **Rule engine only at design level**
  - The business rules section (suppression list, quiet hours, credit checks,
    deduplication) is not implemented as a full rule engine.
  - With more time I would add a small rule pipeline that runs before enqueueing
    a job and marks it as SKIPPED/DELAYED/REJECTED as appropriate.

- **Limited observability**
  - Logging includes basic information, but there is no structured logging library,
    tracing, or metrics backend configured.

---

## 5. What I would change for a real production system

If this system were to move towards production, I would prioritize:

1. **Job/worker model**
   - Introduce `NotificationJob` and `DeliveryAttempt` entities.
   - Process jobs asynchronously using a worker pool or message queue.

2. **Transactional outbox or reliable messaging**
   - Ensure that “campaign created” and “jobs enqueued” events cannot be lost
     if the process crashes after writing to the database.

3. **Idempotency and deduplication**
   - Use unique constraints or a dedicated store to avoid duplicate sends.
   - Store idempotency keys per tenant + campaign + recipient.

4. **Rate limiting and retries**
   - Enforce per‑channel limits (100 req/min/channel).
   - Implement exponential backoff and maximum retry count per job.

5. **Security and multi‑tenancy hardening**
   - Add authentication/authorization around tenant access.
   - Mask PII in logs and secure data at rest.

---

## 6. AI assistance

I used AI in a **supporting role** for:

- Generating some boilerplate classes (DTOs, repositories, configuration).
- Looking up examples of CSV parsing with Apache Commons CSV.

All core service logic (campaign creation flow, CSV integration, error handling)
was reviewed and adjusted by me to match the simplified design above and my
current experience level.***
