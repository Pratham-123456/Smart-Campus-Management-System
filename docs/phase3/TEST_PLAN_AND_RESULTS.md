# Test Plan & Results — Phase 3

**Environment.** Windows 11, Java 21.0.12, Spring Boot 3.3.4, Oracle AI Database 26ai Free in Docker (`oracle-db`, `FREEPDB1`, schema `campus_app`), Chrome headless for screenshots. Run date: **28-Oct-2026**.

| Layer | Tool | Command | Result |
|---|---|---|---|
| Database constraints | SQL*Plus probes (rolled back) | `@database/phase3/03_verify_post_migration.sql` | **23 / 23 passed** |
| Data audit (Findings 1–8) | SQL*Plus | `@database/phase3/01_audit_all.sql` | **0 violations** after migration (before: 3 grades, 51 groups, 102 clashes, 3 + 115 + 24 timeline, 1 late registration, semester precision, 5 + 3 constraints) |
| Unit + integration | JUnit 5, MockMvc, live Oracle, each test rolled back | `mvn test` (with `CAMPUS_DB_PASSWORD` set) | **36 / 36 passed**, 0 skipped; 0 rows left behind |
| End-to-end over HTTP | PowerShell | `powershell -File scripts\verify_workflows.ps1` | **46 / 46 passed** (15 Review 2 + 31 Phase 3); data snapshot identical before/after |
| UI | headless Chrome (CDP) | `node scripts/capture_screenshots.js` | 23 screenshots; data snapshot identical before/after |

Evidence: `docs/phase3/test-evidence/` (`maven_test_output.txt`, surefire reports, `verify_workflows_output.txt`, `sql_audit_before_migration.txt`, `sql_migration_run.txt`, `sql_audit_after_migration.txt`, `data_snapshot_before_after_workflows.txt`).

## 1. JUnit cases (`SmartCampus/src/test/java`)

Fixtures use IDs ≥ 990001 inside the test transaction.

| Group | Test | Rule | Expected | Result |
|---|---|---|---|---|
| Course registration | `validEnrollmentReturns201` | enroll + attendance/marks rows created | 201 | Pass |
| | `sameOfferingTwiceReturns409` | duplicate offering | 409 | Pass |
| | `otherSectionOfSameCourseSameTermReturns409` | **F4** cross-section | 409 | Pass |
| | `fullOfferingReturns409` | seat capacity (2 seats) | 409 | Pass |
| | `unknownOfferingReturns404` | missing offering | 404 | Pass |
| | `dropIsLogicalAndAllowsOtherSectionAfterwards` | drop → DROPPED, marks kept, other section allowed, second drop 404 | 200 / 201 / 404 | Pass |
| | `cannotDropCourseWithPublishedResult` | history protection | 409 | Pass |
| Exam registration | `validDuplicateOverlapAdjacentAndOtherDay` | **F5**: valid 201, duplicate 409, overlap 409, back-to-back 201, other day 201 | as listed | Pass |
| | `unknownExamReturns404` | | 404 | Pass |
| | `notEnrolledReturns409` | must be enrolled in the offering | 409 | Pass |
| | `completedExamIsClosedForRegistration` | | 409 | Pass |
| | `cancelThenReRegisterReusesRow` | cancel → CANCELLED, clash ignores cancelled rows, row reused | 200 / 201 / 409 | Pass |
| Faculty | `attendanceValidNegativeAndAboveHeld` | 0 ≤ attended ≤ held | 200 / 400 / 400 | Pass |
| | `classesHeldCannotDropBelowRecordedAttendance` | | 400 / 200 | Pass |
| | `wrongFacultyGets403` | service-level and interceptor-level | 403 / 403 | Pass |
| | `marksBoundary303050IsAcceptedAndTotalIs100` | **F2**: 30/30/50 → rawTotal 110, totalMarks 100, maxTotal 100, grade S; faculty roster also 100 | 200 | Pass |
| | `rawSumAbove100IsScaledSoTotalStaysWithin100` | raw 102 → total 92.73/100; raw 77 → 70.00 → B | 200 | Pass |
| | `marksAboveCeilingsOrNegativeAre400` | 31 / 30.5 / 51 / −1 | 400 ×4 | Pass |
| Results & re-exams | `seedGradesFixedByMigration` | **F3**: 10002 = B, 10003 = A, 10004 = D | — | Pass |
| | `noLiveExamOrResultViolatesTheTimeline` | **F6** on live data | 0 rows | Pass |
| | `publishAboveMaxMarksIs400AndValidPublishDerivesGrade` | 60/50 → 400; 40/50 → A/PASS; duplicate → 409 | 400 / 201 / 409 | Pass |
| | `futureExamCannotBeCompletedOrHaveResults` | **F6** in the service | 409 / 409 | Pass |
| | `reExamPendingToApprovedThenLocked` | PENDING → APPROVED with Approval_Date; duplicate 409; other student 403; non-admin 403; re-decide 409; withdraw decided 409; unknown 404 | as listed | Pass |
| | `pendingReExamCanBeWithdrawn` | withdraw PENDING; then delete unreferenced result | 200 / 200 | Pass |
| Schema | `semesterOneAndTenAcceptedZeroAndElevenRejected` | **F7** via API and directly in Oracle | 201 / 201 / 400 / 400 / ORA-02290 | Pass |
| | `allFiveStatusChecksAndMarksCeilingsEnforcedByOracle` | **F8** + marks CHECKs | DataIntegrityViolation ×6 | Pass |
| CRUD & access | `departmentLifecycle` | create / update / read / delete / delete-again 404 / update-missing 404 | | Pass |
| | `parentsWithChildrenReturn409` | department, program, course, faculty, student, offering, exam | 409 ×7 | Pass |
| | `unreferencedRecordsCanBeUpdatedAndDeleted` | student, exam, offering, faculty; exam CANCELLED | 200 | Pass |
| | `offeringSeatsCannotGoBelowEnrolled` | seats 0 → 400, seats below enrolled → 409 | | Pass |
| | `unauthenticatedRequestsGet401` | | 401 | Pass |
| | `crossStudentAndNonAdminGet403` | | 403 ×4 | Pass |
| | `errorsNeverLeakSql` | duplicate name → 409 without "ORA-" | | Pass |
| Unit | `MarksPolicyTest` (3) | full marks = exactly 100; 105.7 raw → 96.09 (≤ 100); scaling rounding | | Pass |

## 2. Workflow suite (`scripts/verify_workflows.ps1`)

**Part A** (Review 2 scenarios, made repeatable): A1 login & enrollments; A2 duplicate 409; A3 full capacity 409 (seats temporarily capped, then restored); A4 register → verify → drop → test rows removed; A5/A6 faculty attendance & marks visible to the student (total out of 100) (originals restored); A7 every published result's grade matches marks/Max_Marks; A8 empty results; A9 re-exam apply → PENDING in both views → withdraw; A10 cross-faculty 403; A11 admin lists 10 entity types; A12/A13 responsive tables & filter; A14 validation messages; A15 clean JSON errors.

**Part B** (Phase 3, fixtures created through the API and removed at the end): B1 create department / program / 2 courses / faculty / 2 students / 3 BCrypt accounts; B2 semester 10 accepted; B3 semester 11 → 400; B4–B8 enroll 201 / duplicate 409 / other section 409 / full 409 / unknown 404; B9–B12 exam 201 / duplicate 409 / overlap 409 / back-to-back 201; B13 future exam COMPLETED → 409; B14 publishing a result for a future exam → 409; B15 marks 30/30/50 → total exactly 100/100; B16 final 51 → 400; B17 attendance above held → 400; B18 other faculty → 403; B19 no session → 401; B20 other student → 403; B21 student → admin API 403; B22 update department; B23–B25 deletes with children → 409; B26 unknown → 404; B27 unreferenced exam delete → 200; B28/B29 drop keeps a DROPPED row and cancels the exam registration; B30 every fixture deleted via the API (12 × 200); B31 Oracle constraint probes 23/23.

Full console output: `test-evidence/verify_workflows_output.txt` (**46 / 46 PASSED**).

## 3. SQL audit summary (before → after migration)

| Check | Before | After |
|---|---:|---:|
| F1 live rows vs seeded baseline 1,712 | 1,736 (+24 via portal) | 1,736 |
| F2 component above 30/30/50 | 0 | 0 |
| F2 raw component sums > 100 (informational; shown scaled to /100) | 40 | 40 |
| F3 grades off the GRADE scale | 3 | 0 |
| F4 student–course–term groups with 2+ active sections | 51 | 0 |
| F5 overlapping registration pairs | 102 | 0 |
| F6 future exams marked COMPLETED | 3 | 0 |
| F6 results published before the exam | 115 | 0 |
| F6 results for exams not yet held | 24 | 0 |
| F6 active registrations created after the exam | 1 | 0 |
| F7 Semester precision < 2 | 1 | 0 |
| F8 missing status CHECK constraints | 5 | 0 |
| F2 missing marks ceiling CHECKs | 3 | 0 |

Review 2 reported 18 future COMPLETED exams. That check compares against `SYSDATE`, so the count falls as exam dates pass: on 28-Oct-2026 only the three FATs dated 20-Nov were still in the future.
