# Actual local demonstration screenshots

Captured on 11 October 2026 in Microsoft Edge by `scripts/e2e/verify.cjs`, using the live local backend and an independent MySQL database with fictional data. No responses or screenshots were fabricated. These are portfolio-preparation captures, not screenshots of the 2025 presentation.

- [login.png](login.png): refreshed login page, captured from the live frontend during UI checks.
- [student-home.png](student-home.png): refreshed student dashboard using the fictional account.
- [messaging.png](messaging.png): student conversation after real two-way HTTP messages, receiver polling and reload.
- [forum.png](forum.png): a newly created post, student comment, enterprise reply and persisted sequential like.
- [application-review.png](application-review.png): an application approved after rejection and resubmission.

The script saves full-page screenshots at a 1440 × 1000 browser viewport. Names, example.invalid email addresses, organizations and message contents are fictional. Timestamp suffixes identify test fixtures. Rerunning tests replaces these captures. See the validation record for scope and known failures; chat screenshots do not establish WebSocket delivery.

The October 2026 UI refresh regenerated the three workflow captures with `verify.cjs`; login and student-home were captured separately during real Edge visual checks. The five public captures were visually reviewed. See [UI refresh notes](../UI-REFRESH.md).

## Enterprise module refresh

- [enterprise-tasks.png](enterprise-tasks.png): enterprise task list and actions.
- [enterprise-announcements.png](enterprise-announcements.png): a fictional draft in the announcement list.
- [enterprise-publish.png](enterprise-publish.png): task publishing form.

These actual Edge captures use the unified module styles. The student-home capture now shows the repaired hover border and inset completion label. There are eight public screenshots in total.
