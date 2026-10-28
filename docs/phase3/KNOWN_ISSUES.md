# Known Issues & Honest Notes (Phase 3)

## Data reconciliation decisions (kept on purpose)

1. **58 enrollments are now `DROPPED`, not deleted (Finding 4).** These come from 51 student–course–term groups (109 rows) that held two or three sections of the same course. The migration kept one section per group: the one with the most results, then registrations, then the lowest Enrollment_ID. In **15 groups** the dropped section also has exam registrations or results, and those history rows are kept. They therefore appear as results for a course whose enrollment is `DROPPED`. This is deliberate: deleting or moving them would destroy published academic history. `SELECT * FROM ENROLLMENT WHERE Status = 'DROPPED'` lists them.
2. **Exams were re-timetabled (Findings 5, 6).** The seed had every CAT1 at the same hour (102 overlapping registration pairs), and several exams ran after their results were published. All 42 seed exams moved to unique slots. Published dates and re-exam dates were **not** changed; they are now consistent with the new exam dates.
3. **Three FAT exams (810, 811, 816) are dated 03–04 Oct 2026**, before the other FATs (16–19 Nov). They already had 24 published results and one re-exam decision. Keeping all 190 results timeline-consistent meant placing these three exams before 05 Oct, the date the results were published. Present them as early (accelerated) final assessments for those three courses.
4. **Registration 1133** (student 1001, exam 836) was created through the portal on 14 Oct, after the exam was held on 11 Sep. It had no result and is now `CANCELLED`. The service now rejects registration for exams that have started or are not `SCHEDULED`.
5. **Department 7 "Chemical Engineering"** and re-exam application **11021** were created through the portal before Phase 3. They are counted as portal additions (live 1,736 vs seeded 1,712), not seed data.

## Environment and test notes

6. **JUnit integration tests need Oracle.** `Phase3IntegrationTest` runs against the dev database in a rolled-back transaction. It is skipped (not failed) when `CAMPUS_DB_PASSWORD` is not set. `MarksPolicyTest` always runs.
7. **Workflow suite side effects.** `scripts/verify_workflows.ps1` builds its own fixtures and removes them, restores any seeded values it changes, and was checked with a before/after snapshot (`test-evidence/data_snapshot_before_after_workflows.txt`). Identity values (`MAX(id)+1`) and Oracle sequences can still advance between runs.
8. **During development**, the first draft of the workflow script failed to restore student 1001's offering-401 attendance and marks (an SQL*Plus multi-statement issue, since fixed). The values were reset to the seeded figures (27 classes; 18 / 24 / 42 marks) from `sql/02_insert_sample_data.sql`. If the portal had changed them earlier, that earlier value was not recorded.
9. **ID generation uses `SELECT NVL(MAX(id), n) + 1`** (inherited design). Two truly simultaneous inserts could collide; the PK then raises ORA-00001, which the API returns as 409. Oracle sequences would remove this risk. The change was not made, to avoid altering the schema contract mid-course.
10. **Cross-section uniqueness is enforced in the service layer**, not by an Oracle constraint, because it spans two tables (ENROLLMENT ⋈ COURSE_OFFERING). A compound trigger could enforce it in the database; it is listed as future work.
11. **Grade boundaries are two-decimal bands** (e.g. A 80.00–89.99). Percentages are rounded to two decimals before lookup, so values such as 89.995 % round into the S band.
12. **Screenshots** were captured headlessly at 1440 px width by `scripts/capture_screenshots.js`. In tall captures the dark modal backdrop only covers the first 960 px viewport; this is an artifact of the capture.
13. **`run.bat` holds a dev default database password** (from the original project). Fine for a local course demo; use the `CAMPUS_DB_PASSWORD` environment variable anywhere else.
