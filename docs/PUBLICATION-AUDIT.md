# Public content audit

Initially prepared locally on 11 October 2026. A new source-only Git repository was initialized on 12 October 2026 for publication; original Git history is excluded.

## Excluded by construction

The source copy used an explicit allowlist of source roots and build manifests. It did not copy private uploads, resumes/business licenses, thesis documents/screenshots, original configuration credentials, flyway.conf, raw data dumps, IDE metadata, prior dependency directories, build artifacts or logs. The old SQL repair/migration collection was replaced by a fresh schema baseline; no original database connection or records were used.

## Sanitized or reviewed

- Datasource credentials are environment references; `.env.example` contains placeholders only. Original mail credentials are absent; existing mail functionality is not claimed to work.
- Original password values were compared in memory against prepared text without printing or saving them. No original secret value remains. A coincidental numeric match in a temporary-ID comment was replaced with a generic explanation.
- Static mock contacts and organization names were replaced with fictional labels and reserved example.invalid addresses. Seed records are newly authored fiction, not anonymized real records.
- Author name and confirmed project dates are intentionally retained as portfolio attribution. No private phone number, student identifier, personal email or local user path is intentionally published.
- Account password response fields were made write-only and admin credential prints removed. Other legacy application logging and authentication weaknesses remain; use fictional local data only.
- Database repair/test endpoints and the startup global-setting mutation require the disabled-by-default legacy-maintenance profile. This is limited preparation, not complete security remediation.
- Public non-text assets consist of the build wrapper, small frontend icons and eight newly captured screenshots containing only fictional demonstration data. The Maven wrapper was checked against Maven Central; private document/image/video assets were excluded.

## Recheck

Run `python scripts/check_public_content.py` before a future publication. It reports locations and finding categories without printing matched values. The heuristic checks common token/private-key formats, credential literals, contact strings and private path patterns; it is not a comprehensive secret scanner, malware scanner or application security audit.

Reinspect any future screenshots, SQL changes and uploaded files. The public demo application password is deliberately documented and must not be reused for database access or real accounts. Do not publish local validation artifacts, dependency caches or log files alongside the source directory.

## Runtime evidence added on 11 October 2026

Browser reports contain only local route names, pass/fail observations and synthetic fixture identifiers. Screenshots were visually inspected. Generated database credentials, MySQL data, logs, caches, uploaded test files and machine-specific launch helpers are outside the public source. Publish only the source directory, never its parent workspace. The public scripts contain the intentionally documented fictional application password, not the generated database passwords.
