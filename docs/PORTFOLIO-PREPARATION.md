# Portfolio preparation record

## Original work

Yiming Luo independently completed and demonstrated the undergraduate project between January and June 2025. Messaging, forum and application business code comes from that work. The original project directory is retained unchanged.

## October 2026 preparation

This is a new filesystem copy without Git history. It excludes dependency directories, previous build products, logs, uploaded documents, thesis documents/images, IDE settings, original credential-bearing configuration, and manual/duplicate/destructive migration scripts. Necessary source, static resources, manifests, frontend lockfile and Maven wrapper are retained.

Changes made only in this copy:

1. Replaced private datasource/mail settings with environment-based local configuration and `.env.example`; disabled SQL parameter logging, set schema validation and loopback binding, and aligned both upload property names.
2. Disabled DatabaseConfig and four development/test controllers by requiring the explicit `legacy-maintenance` profile, avoiding accidental table rebuilds and global database settings changes.
3. Removed admin account/password console prints and made account password JSON properties write-only. Plaintext storage and incomplete authorization remain documented limitations.
4. Corrected Vue import path casing to match existing files on case-sensitive systems. Static mock contacts/organization names use clearly fictional replacements. Replaced an invalid `v-model` fallback expression in TrainingMaterialEdit with a writable computed binding; this is a compilation repair, not a claim that the legacy training editor integration works.
5. Added a current-entity schema export utility, a fresh MySQL baseline and synthetic seed. No original database was queried or copied. Selected constraints/indexes from original schema designs are preserved in the baseline. The merged `task_assignment` export was corrected to use its entity ID as the auto-increment primary key and a unique task/student pair; see database notes.
6. Renamed the announcement controller's package-private `ReviewRequest` DTO to `AnnouncementReviewRequest` to resolve an existing duplicate Java type that blocked compilation. Replaced a numeric temporary-ID comment example after a coincidental secret-string scan match.
7. Added README, code-flow notes, local setup, publication audit, validation record, screenshot guidance and ignore rules.

8. Set WebSocketConfig's package to `cn.com.dhc.Config` to match its directory and fix Windows packaged startup. Restricted its allowed origins to the local frontend addresses to repair the observed SockJS handshake failure. Recipient push remains incomplete.
9. Initialized an isolated MySQL 8.0.44 instance, ran the copy, added repeatable Playwright browser/API checks and a separate WebSocket probe, and captured actual screenshots using fictional data. Private local start/stop helpers and generated database credentials remain outside the public repository. The forum counter race found during testing is documented, not repaired or presented as concurrency-safe.

10. Refreshed the login and core workflow UI, added shared responsive styling and keyboard support for home navigation, and regenerated real screenshots after browser regression checks. This visual work was performed in October 2026; see UI-REFRESH.md.

11. Extended the shared UI to task/announcement and enterprise module pages, repaired status-label hover spacing, and replaced obsolete Vue 2 `$set` usage in the enterprise pending-application count. These October 2026 changes and scoped checks are documented in UI-REFRESH.md.

These changes must not be represented as features or engineering achievements completed during the undergraduate period. No new product features, authentication framework, performance optimizations or benchmark claims have been added. The publication audit is a content check, not a guarantee that the legacy application is production secure.
