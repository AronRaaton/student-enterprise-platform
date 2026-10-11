# Database baseline

The public copy uses a fresh **MySQL 8** schema. It does not include a dump, private records, or the original Flyway-style directory. The original build did not configure Flyway and the historical scripts had repeated version numbers and table-drop repairs; do not reconstruct an upgrade path by applying them blindly.

Order from the repository root in the MySQL client:

1. `SOURCE database/00-create-database.sql;`
2. `SOURCE database/01-schema.sql;`
3. Optionally `SOURCE database/02-demo-data.sql;`

Use an empty database. The baseline and seed are not rerunnable upgrades and intentionally do not drop existing tables. The seed is entirely fictional and is labeled October 2026. Email domains are `example.invalid`. Account passwords are public demo strings, not secrets. There are no uploaded resumes, business licenses or real records.

## Provenance

`01-schema.sql` is exported offline from compiled JPA entities using Hibernate's MySQL8 dialect and Spring Boot naming strategies. This uses the existing project dependencies and makes no database connection. The original `task_assignment` table has two mappings: an entity with a generated `id` and a many-to-many join. Raw Hibernate export incorrectly attaches auto-increment to `task_id` in the merged metadata. This baseline explicitly uses `id AUTO_INCREMENT PRIMARY KEY` plus a unique `(task_id, student_id)` pair, allowing both existing insertion paths. This schema-only compatibility correction was made in October 2026; the full baseline and seed were successfully imported into an independent MySQL 8.0.44 instance and passed Hibernate startup validation on 11 October 2026.

A documented appendix preserves the original forum-like/project-application uniqueness and chat lookup indexes; these do not imply concurrency-safe counters or capacity enforcement.

Reproducing the raw entity DDL (PowerShell, repository root, JDK and Maven installed):

```powershell
mvn -DskipTests package dependency:build-classpath '-Dmdep.outputFile=target/runtime-classpath.txt'
$schemaCp = 'target/classes;' + (Get-Content target/runtime-classpath.txt -Raw).Trim()
javac -encoding UTF-8 -cp $schemaCp -d target/schema-tool scripts/ExportSchema.java
java -cp ('target/schema-tool;' + $schemaCp) ExportSchema target/entity-schema.sql
```

The target output must not already exist because Hibernate's exporter appends. This generates the raw mappings only; review the checked-in header and explicit appendix when refreshing the baseline. Do not execute schema-generation tools against a private database.

`spring.jpa.hibernate.ddl-auto=validate` checks mappings at startup instead of silently modifying tables. Additional database constraints are maintained by the baseline; JPA entities do not declare all of them. Some legacy entities map the same join tables, and the fresh 36-table database passed the recorded MySQL execution and application startup validation. See the exact verification status in `docs/VALIDATION.md`.
