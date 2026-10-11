# Backend flow notes

These notes describe code already present in the undergraduate project. Suggested design discussions are not implemented features.

## Messaging transaction boundary

The service commits the JDBC save before the controller attempts STOMP delivery. A successful database save is not an acknowledgement from the receiver. A lost HTTP response can lead the enterprise retry queue to resend a message that already exists, because the backend does not persist a unique client message key. `LAST_INSERT_ID()` is connection-specific; the transactional JDBC connection is relevant to explaining its use.

History queries match both ID directions and sort by timestamp. The frontend slices to 20 only after receiving the whole list. Mark-read updates all unread messages for a sender/receiver pair, not only message IDs known to have been displayed. Fetching history and marking read can race with a newly arriving message. Sender identity, name and timestamp come from the client. Student/enterprise ID namespaces are not disambiguated in the query.

Source: `ChatController.sendMessageHttp`, `ChatService.saveMessage`, `getMessagesBetweenUsers`, `getUnreadMessageCount`, `markMessagesAsRead`; `Views/Chats2e.vue` and `Views/Chate2s.vue`.

Interview discussion: authenticated identity, typed participants/conversation IDs, client message keys, stable cursor ordering, composite indexes, transaction boundaries, polling cost, delivery versus read semantics, and future multi-instance routing. No production throughput or exactly-once guarantee is claimed.

## Forum transaction boundary

`ForumController` delegates to `ForumService`; JPA repositories query and save posts, comments and likes. Post listing is paginated and filters `ACTIVE`. Detail reads increment view counts. Soft-delete changes status. Reply creation loads a parent but does not verify the parent belongs to the same post. DTO conversion only expands immediate children. Like toggling checks for a row, adds/removes it, then changes `likeCount` within a transaction.

The original V21 SQL defined a unique `(post_id, user_id, user_type)` constraint, reproduced in the portfolio baseline. This prevents duplicate rows under that schema, but does not protect denormalized counters from lost updates or provide idempotent toggle semantics. Caller-supplied user identifiers are not proof of ownership.

Interview discussion: idempotent like/unlike endpoints, unique constraints, atomic counter updates versus counting rows, N+1 queries, comment depth, soft-delete consistency and backend authorization.

## Application state flow

The intended UI flow is `PENDING → APPROVED` or `PENDING → REJECTED → PENDING`. `applyForProject` checks existence and existing applications, and checks capacity on new applications. Rejected applications are reused and previous feedback cleared. `reviewApplication` checks capacity for `APPROVED` and then stores the requested string, feedback and timestamp; it does not enforce the intended state machine or verify ownership. `submitProjectStepWithStatus` checks the student's approved application before accepting project work.

The original V17 SQL defined a `(project_id, student_id)` unique constraint, retained in the baseline. It does not prevent two different applicants being approved concurrently for the last place. `@Transactional` alone is insufficient because both transactions can observe spare capacity.

Interview discussion: row locking or atomic capacity reservation, isolation, legal transitions, ownership checks, duplicate requests, audit history and negative/concurrent test cases. These are discussion topics, not completed portfolio improvements.
