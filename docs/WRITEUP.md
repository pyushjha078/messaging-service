## 1. What the AI did vs. what I decided myself

AI handled: project scaffold, Gradle build file, JPA entity boilerplate,
SecurityConfig outline, Swagger annotations, curl examples, test structure.

I decided: the data model (BIGSERIAL cursor over timestamps, participant_key
canonicalization, no @ManyToOne relationships), the 404-not-403 auth policy,
the race-safe conversation creation pattern, the limit+1 pagination trick,
and the structure of the concurrent-insert pagination test.

## 2. Where I overrode the AI's output

Pagination: The AI initially suggested OFFSET-based pagination and sorting
by created_at. Both are wrong here. OFFSET skips rows under concurrent inserts.
Timestamps can collide. I replaced both with cursor-over-BIGSERIAL-id.

Auth response code: The AI returned 403 for non-participants. I changed it to
404. A 403 tells an attacker the conversation id is real.

JPA relationships: The AI added @ManyToOne to entities. I removed them — lazy
loading surprises are a common source of bugs and N+1 queries.

## 3. The three biggest trade-offs

1. H2 in-memory vs. PostgreSQL
   Chose H2 for zero-setup developer experience. Trade-off: H2 doesn't
   fully replicate Postgres behavior (e.g. some constraint names differ).
   For production I'd switch to Postgres + Flyway migrations and run tests
   against a real Postgres via Testcontainers.

2. Auto-create conversation on send vs. explicit POST /conversations
   Chose auto-create: fewer round-trips for clients, simpler API surface.
   Trade-off: slightly muddier REST semantics. The explicit endpoint is
   cleaner REST but forces clients to do two calls for a first message.

3. JPQL subquery for "last message" vs. denormalized last_message_id
   Chose the subquery to keep the write path simple — one INSERT, no
   UPDATE on conversations. Trade-off: slightly slower reads at scale.
   If reads dominate, denormalize last_message_id and update it on every
   message insert.

## 4. What's missing / what I'd do with another day

- Cursor pagination on the conversation list (currently returns up to 50)
- Read receipts and unread counts
- WebSocket push for real-time delivery
- Rate limiting on POST /messages (e.g. 60/min/user with Bucket4j)
- Soft delete and message edit history
- Switch to Postgres + Flyway + Testcontainers for full production parity
- Structured JSON logging + distributed tracing