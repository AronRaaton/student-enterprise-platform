# Local browser and transport verification

Added during October 2026 portfolio preparation. Requires Node 24 (used here), a running fictional demo database seeded from the public SQL files, backend at localhost:8080, frontend at localhost:5173, and installed Microsoft Edge. Tests are restricted to loopback URLs and use the public demo accounts. They create fictional rows and update seeded read state/submission files; do not point them at valuable data.

From this directory:

```sh
npm install --ignore-scripts
npm test
node verify-websocket.cjs
```

The manifest pins Playwright 1.62.1. This verification used a pre-existing isolated installation; no project-local npm install is claimed. The default browser channel is `msedge`, so a Playwright browser download is not needed if Edge is installed. `PLAYWRIGHT_MODULE` can optionally reference an existing Playwright module. `BROWSER_CHANNEL`, `DEMO_WEB_URL` and `DEMO_API_URL` override the defaults; browser-origin configuration must also allow the selected frontend URL.

- `verify.cjs`: real browser logins, two-way HTTP chat, reload/history, unread/read-state checks, forum posting/comments/replies/sequential likes, application rejection/resubmission/approval, and targeted API negatives/file round-trip. Writes `docs/browser-validation.json` and three real screenshots. Failed grouped checks set a nonzero exit code. Browser observations are diagnostic, not an exhaustive request audit.
- `verify-websocket.cjs`: actual STOMP connection, both known user-subscription paths, HTTP-triggered delivery observation for five seconds, and STOMP inbound persistence. Writes `docs/websocket-validation.json`. Inspect `verdict`: script completion does not mean recipient delivery passed. Current recorded outcome is `NOT_DELIVERED`.

No network responses are mocked. Each run appends uniquely labeled fictional records. Tests do not constitute comprehensive regression, security or load coverage. Raw private logs are deliberately excluded from the public repository.
