# Smart Campus Management System — Phase 3 Implementation Report

**Course:** BACSE202 Database Systems (Fall 2026) · **Assessment 10:** Phase 3 — Integration & Delivery (10 marks, CO5)
**Project:** XYZ University Academic Services Portal (VTOP-inspired) · **Report date:** 28-Oct-2026

---

## 1. Team

| Name | Register No. |
|---|---|
| Pratham Garg | 25BCE2016 |
| S Rohith | 25BCE0790 |
| Divyam Kurkan | 25BCE2041 |

## 2. Abstract

A web portal for students, faculty and administrators backed by a normalised (3NF) Oracle 26ai database of 15 tables and 20 foreign keys. Phase 3 closes all eight Review 2 Appendix A findings. It also completes create/read/update/delete (or logical retirement) for every table over JDBC, and verifies the system with SQL audits, 36 JUnit tests, a 46-scenario HTTP workflow suite and 23 screenshots.

## 3. Problem statement

University academic records (enrollments, attendance, continuous assessment, exams, results, re-evaluations) are usually spread across spreadsheets, with no integrity guarantees. The project gives each role one consistent, constraint-checked source of truth.

## 4. Objectives

1. A 3NF relational schema with declarative integrity (PK, FK, UNIQUE, CHECK).
2. Role-isolated web access to that schema through parameterised JDBC.
3. Business rules (capacity, clashes, ceilings, timelines) enforced both in the service layer and, where possible, in Oracle.
4. Reproducible verification and documentation.

## 5. Scope and roles

| Role | Can do |
|---|---|
| Student | register/drop courses, view attendance %, marks (total out of 100), grades and drop, register/cancel exams, view published results, apply for and withdraw re-exams |
| Faculty | view assigned offerings and rosters, set classes held, record attendance and marks |
| Administrator | live KPIs; full management of departments, programs, courses, offerings, students, faculty, exams, results, re-exam decisions and user accounts |

## 6. Technology stack

Java 21 · Spring Boot 3.3.4 (web, jdbc, validation) · `JdbcTemplate` (PreparedStatements) · HikariCP · Oracle AI Database 26ai Free (Docker `oracle-db`, PDB `FREEPDB1`) · ojdbc11 · Spring Security Crypto (BCrypt) · HTML5/CSS3/vanilla JavaScript SPA · JUnit 5 + MockMvc · PowerShell · Node (CDP screenshots).

## 7. Architecture

```
Browser (SPA: index.html, api.js, student.js, faculty.js, admin.js, admin-crud.js)
   │  fetch(), JSESSIONID cookie
   ▼
AuthInterceptor ── 401 no session · 403 wrong role / other user's id
   ▼
REST controllers (Auth, Student, Faculty, Admin) + GlobalExceptionHandler (400/403/404/409/500 JSON)
   ▼
Services (business rules, @Transactional)
   ▼
JDBC repositories (JdbcTemplate → PreparedStatement, ? binds only)
   ▼
HikariCP (max 10, min idle 2) → Oracle 26ai FREEPDB1 (15 tables, 20 FKs, 11 named CK_ constraints)
```

No ORM is used: every SQL statement is visible in the repositories and binds values only through `?` placeholders, which rules out SQL injection.

## 8. Normalisation (1NF → 2NF → 3NF)

* **1NF.** Atomic attributes; repeating course and exam data were split into ENROLLMENT, EXAM_REGISTRATION and EXAM_RESULT rows.
* **2NF.** Every non-key attribute depends on the whole key. Tables with composite natural keys (Student, Offering) carry surrogate PKs plus UNIQUE (Student_ID, Offering_ID).
* **3NF fixes made in Review 1/2:**
  * `Classes_Held` depends on the offering, not on a student–offering pair, so it moved from ATTENDANCE to COURSE_OFFERING. Attendance % = Classes_Attended / Classes_Held × 100 is computed, never stored.
  * `RE_EXAM_APPLICATION` does **not** store Student_ID, which would be a transitive dependency via EXAM_RESULT → EXAM_REGISTRATION → STUDENT; the student is resolved by joins.
  * Grades are looked up from GRADE (percentage bands) rather than duplicated as rules in each row.

## 9. Schema: 15 tables, 20 foreign keys

DEPARTMENT, PROGRAM, STUDENT, FACULTY, COURSE, COURSE_OFFERING, ENROLLMENT, ATTENDANCE, MARKS, GRADE, EXAM, EXAM_REGISTRATION, EXAM_RESULT, RE_EXAM_APPLICATION (14 academic tables, 18 FKs) plus USER_ACCOUNT (2 FKs) = **15 tables, 20 FKs** (verified with `USER_CONSTRAINTS`, constraint_type 'R').

## 10. Integrity constraints added in Phase 3

| Constraint | Table | Rule |
|---|---|---|
| Semester `NUMBER(2)` + CHECK 1..10 | COURSE_OFFERING | semester 10 can now be stored |
| CK_ENROLLMENT_STATUS | ENROLLMENT | ENROLLED, DROPPED, COMPLETED |
| CK_EXAM_STATUS | EXAM | SCHEDULED, COMPLETED, CANCELLED |
| CK_EXAMREG_STATUS | EXAM_REGISTRATION | REGISTERED, CANCELLED |
| CK_RESULT_STATUS | EXAM_RESULT | PASS, FAIL, WITHHELD |
| CK_REEXAM_STATUS | RE_EXAM_APPLICATION | PENDING, APPROVED, REJECTED |
| CK_MARKS_CA / _MIDTERM / _FINAL | MARKS | 0–30, 0–30, 0–50 |

The vocabularies are the values used by the seed data and the Java code; nothing else is accepted.

## 11. Dataset: 1,712 seeded rows vs live rows (Finding 1)

The seeded baseline is **1,712 rows**: 1,636 academic rows (45 in `02_insert_sample_data.sql` + 1,591 in `03_expand_sample_data.sql`) and 76 USER_ACCOUNT rows from `04_authentication_migration.sql` (1 admin + 60 students + 15 faculty). The **1,896** figure quoted in Review 2 is the *line* count of `dataset_export.txt`, which includes headers and separators. It has been corrected in README, FILE_DIRECTORY_REPORT and the HTML documentation.

`database/phase3/01_audit_row_counts.sql` reports the live database:

| Table | Seeded | Live | Added via portal |
|---|---:|---:|---:|
| DEPARTMENT | 6 | 7 | 1 |
| ENROLLMENT / ATTENDANCE / MARKS | 365 / 365 / 250 | 372 / 372 / 257 | 7 / 7 / 7 |
| EXAM_REGISTRATION | 232 | 233 | 1 |
| RE_EXAM_APPLICATION | 20 | 21 | 1 |
| all other tables | = | = | 0 |
| **Total** | **1,712** | **1,736** | **24** |

## 12. Marks policy: course total out of 100 (Finding 2)

The component ceilings stay **CA 30, Midterm 30, Final 50**. Existing marks such as a Final of 48.1/50 depend on them, and 40 seed rows have a raw sum above 100. **The course total is always out of 100:** total = (CA + Midterm + Final) × 100 ÷ 110, rounded to 2 decimals. Full marks give exactly 100.00, and a raw 105.7 gives 96.09. Everywhere (`Marks` model, faculty roster DTO, student marks page, grade sheet, faculty roster) each component is shown with its ceiling and the total as **x / 100**. The raw sum is still returned as `rawTotal` (/110) for transparency. The course grade is looked up from GRADE using the /100 total; previously the raw sum was wrongly treated as a percentage. Exam results are graded separately as Marks_Obtained / EXAM.Max_Marks × 100. Ceilings are enforced three times: by DTO `@DecimalMax`, by the service, and by Oracle `CK_MARKS_*`.

## 13. Appendix A fixes (summary)

| # | Finding | Code | Live DB | Verified by |
|---|---|---|---|---|
| 1 | Row count 1,712 vs 1,896 | docs corrected | audit script | `01_audit_row_counts.sql` output |
| 2 | Marks total (now /100) | Marks model, DTO, UI, grading | `CK_MARKS_*` | JUnit ×3, PS A6/B15/B16, probes |
| 3 | Off-scale seed grades | seed `02_…` | 10002 = B, 10003 = A, 10004 = D | JUnit, audit (0 mismatches), PS A7 |
| 4 | Cross-section duplicate enrollment | `StudentService.registerCourse` → 409 | 58 rows logically DROPPED | JUnit, PS B6, audit 51 → 0 |
| 5 | Clashing exam registrations | `StudentService.registerExam` → 409; back-to-back allowed | 42 exams re-timetabled | JUnit, PS B11/B12, audit 102 → 0 |
| 6 | Results before exam / future COMPLETED | `AdminService.updateExamStatus`, `publishResult` → 409; registration closed after start | timetable + status reconciliation | JUnit, PS B13/B14, audit 115 + 24 + 3 → 0 |
| 7 | Semester NUMBER(1) | DTO/service 1..10 | `NUMBER(2)` | JUnit, PS B2/B3, probes |
| 8 | Unchecked status columns | service vocabularies | 5 named CHECKs | JUnit, probes |

Details of each reconciliation decision are in `KNOWN_ISSUES.md`.

## 14. Business rules (service layer)

* **Course registration:** 404 unknown offering; 409 same offering; 409 another section of the same course in the same semester and year (Finding 4); 409 at capacity; a dropped row is re-activated instead of inserting a duplicate.
* **Drop:** logical DROPPED; cancels pending exam registrations of that offering; 409 once results exist.
* **Exam registration:** 404 unknown; 409 duplicate; 409 not enrolled; 409 exam not SCHEDULED or already started; 409 on overlap `newStart < existingEnd AND existingStart < newEnd` on the same date (strict, so back-to-back is allowed).
* **Attendance:** 0 ≤ attended ≤ Classes_Held; Classes_Held cannot drop below an attended value already recorded.
* **Results:** only for COMPLETED exams whose end time has passed; 0 ≤ marks ≤ Max_Marks; grade and PASS/FAIL derived from GRADE; WITHHELD hidden from students; a result with a re-exam application cannot be deleted.
* **Re-exam:** only on one's own non-withheld result; one per result; PENDING → APPROVED/REJECTED once (Approval_Date set); PENDING can be withdrawn.

## 15. JDBC and HikariCP

Repositories use `JdbcTemplate.query/update/queryForObject`. Each call obtains a pooled connection, prepares the statement with `?` placeholders, binds values, maps rows with lambdas and returns the connection. `@Transactional` services share one connection per business operation, so the checks and the write commit or roll back together. HikariCP is configured with a maximum of 10 connections, 2 minimum idle, a 20 s connection timeout and a 20 min max lifetime.

## 16. REST API and HTTP status codes

Creates return **201**, reads/updates/deletes **200**. Errors return a JSON `ApiResponse` `{success:false, message}`: **400** validation / out-of-range / malformed JSON, **401** no session, **403** wrong role or another user's record, **404** unknown id, **409** duplicate, conflict, timeline or FK children, **500** generic (logged server-side, never exposing SQL). The full endpoint list per table is in `CRUD_COVERAGE.md`.

## 17. Security

BCrypt password hashes (hashes are never returned by the API); session-based authentication; `AuthInterceptor` role isolation and same-user checks on `/api/student/{id}` and `/api/faculty/{id}`; faculty may only touch offerings assigned to them; the last active administrator cannot be deactivated or deleted; parameterised SQL only; toasts render server messages as text, not HTML.

## 18. CRUD coverage

All 15 tables have create, read, update and delete or logical retirement, wired from UI to Oracle. See `CRUD_COVERAGE.md` for the matrix (endpoints, controller/service/repository, roles, constraints, tests).

## 19. User interface

The Student, Faculty and Admin portals show live data only (no hardcoded counts). The admin navigation now exposes Programs, Offerings, the Faculty Directory and User Accounts, which were previously reachable only after creating a record. The provisioning console had called two API functions that did not exist; both are now defined. Every table has row actions; toasts show `[status] message`; buttons lock while a write is in flight.

## 20. Database migration

`database/phase3/01_audit_*.sql` (read-only) → `02_migrate_schema_and_data.sql` (idempotent; re-run verified: every step 0 rows / already present) → `03_verify_post_migration.sql` (23 rolled-back probes). Fresh installs get the same state from `sql/01–04`. Procedure: `MIGRATION_GUIDE.md`.

## 21. Testing strategy

Four layers: Oracle constraint probes, SQL data audits, JUnit/MockMvc integration tests against the real database (transaction rolled back), and an HTTP workflow suite with self-cleaning fixtures. Screenshots are captured automatically. Plan and case list: `TEST_PLAN_AND_RESULTS.md`.

## 22. Test results

| Suite | Result |
|---|---|
| Oracle constraint probes | 23 / 23 |
| SQL audit after migration | 0 violations (13 checks) |
| JUnit (`mvn test`) | 36 / 36 |
| `verify_workflows.ps1` | 46 / 46, live data unchanged |

## 23. Screenshots

`docs/phase3/screenshots/` contains 23 PNGs: login; the student dashboard, registration (live seats), **attendance %**, **marks & grade history (total /100)**, exam schedule, **published results** and **re-exam applications** (the four screens missing on Review 2 Slide 10); 409/403/400 toasts; faculty offerings, roster, marks modal; admin KPIs, offerings CRUD, edit modal, FK-protected delete 409, exam timetable actions, result-publication modal, future-exam 409, re-exam decisions, user accounts. Regenerate with `node scripts/capture_screenshots.js`.

## 24. How to run

1. Start Docker Desktop. Then run `run.bat`, which starts `oracle-db`, sets the database password and launches the jar on :8080.
2. For an existing database, apply `database/phase3` (see `MIGRATION_GUIDE.md`).
3. Run the tests: `cd SmartCampus` and `mvn test` (needs the `CAMPUS_DB_PASSWORD` environment variable).
4. Run `powershell -ExecutionPolicy Bypass -File scripts\verify_workflows.ps1`.
5. Dev logins: student `1001`, faculty `201`, admin `admin` (dev password `1234`).

## 25. Limitations and future work

Cross-section uniqueness could also be enforced by a compound trigger; sequence- or identity-based keys would replace `MAX(id)+1`; CGPA across semesters; audit logging of admin changes; HTTPS and CSRF tokens for production. See `KNOWN_ISSUES.md`.

## 26. Conclusion

The schema is in 3NF with 20 FKs and 11 named CHECK constraints. The Review 2 findings are fixed in the code, the seed scripts and the live database, each with an audit that now reads zero. Every table is reachable end-to-end through JDBC with correct HTTP semantics. Every claim in this report is backed by an evidence file in `docs/phase3/test-evidence/`.
