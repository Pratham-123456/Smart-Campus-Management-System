# Phase 3 Migration Guide

Two paths lead to the same end state:

| Situation | Use |
|---|---|
| Existing, populated Oracle 26ai container (`oracle-db`, `FREEPDB1`) | `database/phase3/01 → 02 → 03` (below). Idempotent; no DROP, TRUNCATE or DELETE |
| Brand-new database | `sql/01_create_schema.sql` → `sql/02_insert_sample_data.sql` → `sql/03_expand_sample_data.sql` → `sql/04_authentication_migration.sql` (already contain every Phase 3 fix) |

## What the migration changes

| Step | Finding | Change |
|---|---|---|
| 1 | F7 | `ALTER TABLE COURSE_OFFERING MODIFY (Semester NUMBER(2))`, only if precision < 2 |
| 2 | F3 | `EXAM_RESULT` 10002 → B, 10003 → A, 10004 → D (only if different) |
| 3 | F5, F6 | `MERGE` re-timetables the 42 seed exams into unique, non-overlapping slots: CAT1 07–12 Sep, CAT2 28 Sep–02 Oct, the three FATs with published results on 03–04 Oct, the other FATs 16–19 Nov. Only exams dated before today keep `COMPLETED` |
| 4 | F6 | Safety nets: future exams cannot stay `COMPLETED`; any result still dated before its exam moves to exam date + 7; registrations created after their exam (no result) become `CANCELLED` |
| 5 | F4 | Cross-section duplicates: per (student, course, semester, year) keep the section with the most results → registrations → lowest Enrollment_ID; the rest become `Status = 'DROPPED'` (no row deleted) |
| 6 | F8, F2 | Adds `CK_ENROLLMENT_STATUS`, `CK_EXAM_STATUS`, `CK_EXAMREG_STATUS`, `CK_RESULT_STATUS`, `CK_REEXAM_STATUS`, `CK_MARKS_CA`, `CK_MARKS_MIDTERM`, `CK_MARKS_FINAL` (only if missing) |

## Applying it to a populated container

The commands below type the password at the SQL*Plus prompt, so it never appears in shell history. Run them from the repository root.

```bash
docker cp database/phase3 oracle-db:/tmp/phase3
```

```bash
docker exec -it oracle-db sqlplus campus_app@//localhost:1521/FREEPDB1
```

Then, at the `SQL>` prompt:

```sql
@/tmp/phase3/01_audit_row_counts.sql
@/tmp/phase3/01_audit_all.sql
@/tmp/phase3/02_migrate_schema_and_data.sql
@/tmp/phase3/03_verify_post_migration.sql
@/tmp/phase3/01_audit_all.sql
```

Expected results:

* `02_…` prints `Added CK_…` eight times on the first run and `… already present` / `0 rows updated` on every later run.
* `03_…` ends with `CONSTRAINT PROBES: 23 passed, 0 failed`. Every probe is rolled back.
* The second `01_audit_all.sql` shows **0** in every violation row. The row reporting 40 MARKS rows with a total above 100 is informational: it counts raw component sums (maximum 110), which the application scales to a total out of 100.

The run recorded on 28-Oct-2026 is in `docs/phase3/test-evidence/` (`sql_audit_before_migration.txt`, `sql_migration_run.txt`, `sql_audit_after_migration.txt`).

## Rollback notes

* Everything except the column widening is plain data or constraint DDL. To undo a constraint, run `ALTER TABLE … DROP CONSTRAINT CK_…`.
* Previous exam times are recorded in git history: `git show HEAD~1:sql/03_expand_sample_data.sql`. The DROPPED enrollments can be listed with `SELECT * FROM ENROLLMENT WHERE Status = 'DROPPED'`.
* `NUMBER(1)` → `NUMBER(2)` cannot be narrowed back while semester 10 exists; it also does not need to be.

## After migrating

Rebuild and restart the application, because the Java code depends on the new statuses:

```bash
cd SmartCampus && mvnw.cmd -DskipTests package
```

Then start `run.bat` (or `run.ps1`) again.
