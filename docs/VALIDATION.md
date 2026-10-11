# Validation record

Date: **11 October 2026 (Asia/Shanghai)**. These checks apply to the October 2026 portfolio edition, not the original January–June 2025 development or demonstration. All runtime data is fictional.

## Environment and isolation

- Windows; Microsoft OpenJDK 17.0.17; Maven 3.9.11; Node 24.19.0; isolated npm 10.9.2.
- MySQL **8.0.44**, using the existing installed executable with a **new data directory**, loopback port **33307**, and no service installation. The existing MySQL80 service and original project database were not modified or used.
- Spring Boot **2.7.13** on 127.0.0.1:8080; Vue production assets served by Vite **6.2.0** preview on 127.0.0.1:5173.
- Actual Microsoft Edge browser controlled by Playwright **1.62.1**, separate contexts for each role. No mocked API responses. Screenshots use a 1440 × 1000 viewport and full-page capture.
- Generated database credentials, data files, runtime uploads, logs, dependencies and launch helpers remain outside the public source. The application account has SELECT/INSERT/UPDATE/DELETE on the demo schema only.

## Completed checks

| Check | Observed result and scope |
| --- | --- |
| Backend compilation/package | Passed with `mvn -o -DskipTests package` using the isolated dependency cache. Main/test sources compiled; JUnit execution skipped. |
| Frontend dependencies/build | `npm ci --ignore-scripts --no-audit --no-fund` and `npm run build` passed. Existing chunk-size warning remains. |
| MySQL initialization | `00-create-database.sql`, `01-schema.sql`, `02-demo-data.sql` imported in order with exit code 0. Independent SQL query returned 36 tables and server version 8.0.44. |
| Backend startup | Spring Boot started and Hibernate `ddl-auto=validate` accepted the schema using the DML-only account. |
| Frontend startup | Vite preview served the real built application. Student and enterprise browser logins succeeded. |
| Login negatives | Incorrect password returned HTTP 400; successful student login JSON omitted the password field. This is not proof of secure authentication. |
| Messaging | Student → enterprise and enterprise → student via the actual UI. Receiver polling showed messages; reload and history API confirmed saved records. |
| Unread | Sending increased the API unread count; opening the student conversation marked messages read, with persisted read state checked. |
| Forum | Created post, comment and direct reply in separate role contexts; sequential like/unlike/like produced counts 1/0/1; reload preserved the visible reply and liked state. |
| Applications | Created a fictional project, submitted as student B, rejected with feedback, resubmitted the same application row, approved as enterprise, then reloaded. Duplicate pending submission was rejected. |
| Upload/download | Multipart upload of a fictional text file for seeded submission 1; downloaded bytes matched. API test only, not the browser upload UI. |
| Disabled maintenance route | `/api/db-fix/check-chat-message-table` returned 404 without the legacy profile. |
| Restart and database persistence | Stopped and restarted the isolated MySQL, backend and frontend; independent SQL queries confirmed both browser messages, two forum comments, one like with count 1, and the APPROVED application persisted. See [database-validation.json](database-validation.json). |
| Original integrity | Rechecked SHA-256 hashes of all 299 original source/configuration files against the preparation snapshot: unchanged. |
| Public credential check | No generated root/application database password values found in reviewed public text; values were not printed. |
| Screenshots | Three actual browser captures saved and visually inspected. Only fictional names, messages, contact addresses and project records are shown. |

The final browser/API run passed **9 grouped checks**. Its browser observers recorded no JavaScript page errors or failed observed API responses. This is neither a coverage figure nor proof that every request/path was observed. Evidence: [browser-validation.json](browser-validation.json), [scripts](../scripts/e2e/README.md), [screenshots](screenshots/README.md).

## WebSocket: connection succeeds, recipient push does not

The initial `/ws/info` call returned 500 because wildcard allowed origins conflicted with credentialed SockJS CORS. After restricting the endpoint to the two local frontend origins:

1. `/ws/info` returned 200 and a real browser WebSocket completed STOMP CONNECT.
2. The probe subscribed to `/user/queue/messages` and the legacy `/user/2001/queue/messages` path.
3. An HTTP send persisted the message, but **no corresponding MESSAGE frame arrived during a five-second observation window**, with no receiver history polling.
4. A STOMP SEND to `/app/chat.sendMessage` also persisted successfully, verified separately through history.

[websocket-validation.json](websocket-validation.json) records `connected: true`, both persistence checks true, and `recipientDelivered: false`. A successful diagnostic script exit means the probe ran, not that delivery passed.

`ChatController` uses user-destination sends, while `WebSocketConfig` does not establish an authenticated account Principal for those destinations. The enterprise Vue page's WebSocket imports are commented out. The working browser demonstration therefore remains **HTTP messaging with polling**. This work does not add or claim a complete secure push implementation.

## Problems discovered during verification

- The initial backend startup failed because WebSocketConfig's lowercase package did not match its uppercase Config directory on Windows packaging. The portfolio copy now uses `cn.com.dhc.Config`.
- The wildcard SockJS CORS setting prevented handshakes. It now permits localhost:5173 and 127.0.0.1:5173 explicitly.
- The first forum check overlapped a detail GET (which saves an incremented view count) with a like write. The like row existed while the stored post like count was 0. This is consistent with the source's whole-entity read-modify-write race. The final normal-flow check waits for each like response before subsequent reads; **the concurrency defect remains**. No concurrency-safety claim is made.

These are October 2026 preparation findings/fixes. See [preparation record](PORTFOLIO-PREPARATION.md).

## Remaining unverified or incomplete

- A complete secure WebSocket recipient-delivery path; acknowledgements, retry deduplication, reconnect behavior, offline delivery and multi-instance routing.
- Full authentication/authorization hardening, all roles and unrelated modules, browser upload UI, exhaustive failure paths and concurrent capacity enforcement.
- Original JUnit execution, comprehensive regression coverage, latency/throughput/load testing, penetration testing and dependency vulnerability audit.
- Linux deployment, external hosting, clean-machine wrapper bootstrap, email/education-service integration and placeholder training features.

## Reproduction

For a new machine, use the repository README to initialize a disposable MySQL database and start both applications. The local verification used port 33307 by setting DB_URL; the default setup uses 3306. Then follow [scripts/e2e/README.md](../scripts/e2e/README.md). Test scripts add fictional rows and overwrite evidence screenshots/reports; use a disposable demo database. They do not reset or migrate a private database.

Final heuristic public-content scan: **317 text files, 0 findings**. Generated database password comparison also found no public-text matches. All 299 files in the original source/configuration hash snapshot remained unchanged. Build output was moved outside the public source after verification. The private local stop/start helpers were exercised successfully; they start the existing initialized demo without reimporting seed data. No GitHub publication was performed.

## UI refresh follow-up

The October 2026 UI refresh passed a new frontend production build and the same nine grouped browser/API checks. Six pages were checked at desktop and mobile widths (12 combinations), with no document-wide horizontal overflow. Real business screenshots were refreshed and two presentation screenshots added. See [UI-REFRESH.md](UI-REFRESH.md). Previous WebSocket diagnostic results and backend limitations are unchanged.

## Enterprise module UI follow-up

Final records completed on 12 October 2026. The shared UI covers 54 components; 68 page/viewport checks fit the viewport. All nine grouped browser/API checks passed again. See [module-ui-validation.json](module-ui-validation.json) and [UI-REFRESH.md](UI-REFRESH.md) for scope and the observed project-plan-review HTTP 500 limitation.
