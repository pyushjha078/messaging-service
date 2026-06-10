# Decision Log

## 1. H2 in PostgreSQL mode
**Decision:** Use `H2` with `MODE=PostgreSQL` for dev and test, making it easier to swap to a real PostgreSQL database in production.
**Rationale:** Zero infrastructure overhead during development while keeping the SQL dialect close to production.

## 2. `ParticipantKey` as a commutative composite key
**Decision:** `ParticipantKey.canonical(a, b)` always sorts the two `UUID`s so that `canonical(a, b) == canonical(b, a)`.
**Rationale:** Guarantees at most one conversation per pair without requiring application-level locking. The unique constraint on `participant_key` enforces this at the database level.

## 3. Cursor-based pagination, not offset-based
**Decision:** Use `id < cursor` with `ORDER BY id DESC` instead of `OFFSET/LIMIT`.
**Rationale:** Offset-based pagination skips or duplicates rows when new messages are inserted mid-walk. Cursor-based pagination is stable regardless of concurrent writes.

## 4. `404` instead of `403` for unauthorized conversation access
**Decision:** Return `404` when a non-participant tries to read a conversation.
**Rationale:** Returning `403` would leak the existence of a conversation the user should not know about. `404` gives the same response whether the conversation does not exist or the user cannot see it.

## 5. `JWT` in `Authorization` headers, not cookies
**Decision:** Use stateless `JWT`s in the `Authorization: Bearer <token>` header.
**Rationale:** No server-side persistence is needed, and a short `TTL` limits the damage window of a leaked token.

## 6. Conversation auto-creation on first message
**Decision:** A conversation is implicitly created when the first message is sent between users.
**Rationale:** Eliminates a separate create-conversation endpoint and its associated race-condition handling. The `ON CONFLICT` unique constraint on `participant_key` handles concurrent first-message scenarios.

## 7. Global exception handler with structured error codes
**Decision:** All errors return JSON in the form `{"code": "ERROR_CODE", "message": "..."}`.
**Rationale:** Machine-readable error codes let clients branch on specific failure modes without parsing human-readable strings.
