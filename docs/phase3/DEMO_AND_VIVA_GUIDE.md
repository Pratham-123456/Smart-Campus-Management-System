# Demo Script (≈5 min) & Viva Question Bank

## Before the demo

1. Docker Desktop running → `run.bat` → browser on `http://localhost:8080`.
2. Open SQL*Plus in a second window: `docker exec -it oracle-db sqlplus campus_app@//localhost:1521/FREEPDB1` and enter the password when prompted.
3. Dev logins: student `1001`, faculty `201`, admin `admin`, all with the dev password `1234`.

Every demo step below either reads data or is **rejected** by the server, so the database is unchanged afterwards. Step 3c is the exception: it writes, and step 3d undoes it.

## 1. Student (≈1.5 min)

| Click | Expect | SQL*Plus check |
|---|---|---|
| Student portal → 1001 / 1234 | Profile of Aarav Sharma | — |
| Academics → Attendance Record | % per course = attended / held (computed) | `SELECT a.Classes_Attended, o.Classes_Held FROM ATTENDANCE a JOIN COURSE_OFFERING o ON o.Offering_ID = a.Offering_ID WHERE a.Student_ID = 1001;` |
| Continuous Assessment & Marks | `/30 /30 /50`, total out of 100 (e.g. 105.7 raw → 96.09), grade, Drop button | `SELECT Continuous_Assessment_Marks + Midterm_Marks + Final_Exam_Marks FROM MARKS WHERE Student_ID = 1001;` |
| Course Offerings & Registration → **Register Now** on #404 (Database Systems Sec B) | Red toast **[409] Already enrolled in another section… #401** | `SELECT Status FROM ENROLLMENT WHERE Student_ID = 1001 AND Offering_ID = 404;` → no row |
| Examinations → Published Results | Results with grades; 10003 shows **A** (42/50 = 84 %) | `SELECT Grade_Code FROM EXAM_RESULT WHERE Exam_Result_ID IN (10002, 10003, 10004);` → B, A, D |
| Re-Examination Applications | Status badges PENDING/APPROVED/REJECTED | — |

## 2. Faculty (≈1 min)

| Click | Expect |
|---|---|
| Sign out → Faculty 201 / 1234 → Assigned offerings → open #401 | Roster with attendance % and totals shown as `x / 100` |
| **Marks** for 1001 → set CA to 35 → Save | Browser blocks it (max 30); bypassing the form, the API returns **[400] CA marks cannot exceed 30** |
| **Attendance** → enter 31 when held is 30 | **[400] Classes attended (31) cannot exceed total classes held (30)** |

## 3. Administrator (≈2 min)

| Click | Expect |
|---|---|
| Admin / 1234 → Academic Overview | Live KPI tiles (students 60, faculty 15, offerings 45 …) |
| a. Departments → **Delete** on Computer Science | **[409] … still referenced by N program(s), … Academic history is never cascade-deleted.** |
| b. Examinations → Schedule → **Mark Completed** on any FAT dated 16–19 Nov | **[409] … cannot be marked COMPLETED before it has been held.** |
| c. Examinations → **Results** on #801 → change 1002's marks to 41 → Update | Grade re-derived from GRADE (82 % → A); the toast shows the server message |
| d. Change it back to 38 → Update | Grade returns to **B**. Note: re-publishing sets Published_Date to today |
| Course Offerings → **Edit** on #401 → set Semester 11 → Save | The form blocks it (max 10); the API on its own answers **[400] Semester must be between 1 and 10** (`verify_workflows.ps1` B3) |

Close with: `@/tmp/phase3/01_audit_all.sql` → every violation row reads 0.

## 4. Viva question bank

**Normalisation & design**
1. *Why is Classes_Held in COURSE_OFFERING?* It depends only on the offering (Offering_ID → Classes_Held). Storing it per student–offering pair caused a partial/transitive dependency and update anomalies, so moving it is a 3NF fix.
2. *Why no Student_ID in RE_EXAM_APPLICATION?* Exam_Result_ID → Exam_Registration_ID → Student_ID; storing it would be a transitive dependency that could drift out of sync.
3. *Is attendance % stored?* No. It is derived (attended / held × 100) at query time, so it can never become stale.
4. *Name some functional dependencies.* Student_ID → Name, Email, Program_ID; Offering_ID → Course_ID, Faculty_ID, Semester, Year, Section, Max_Seats, Classes_Held; (Student_ID, Offering_ID) → Classes_Attended; Grade_Code → Grade_Point, Min_%, Max_%.
5. *Why surrogate keys plus UNIQUE (Student_ID, Offering_ID)?* Stable single-column FK targets, while the UNIQUE constraint keeps the natural key unique.
6. *How many tables and FKs?* 15 tables and 20 FKs (14 academic tables with 18 FKs, plus USER_ACCOUNT with 2).

**Appendix A**

7. *1,712 vs 1,896?* 1,712 seeded rows (1,636 academic + 76 logins). 1,896 is the line count of `dataset_export.txt`.
8. *The components add up to 110, so how is the total out of 100?* The ceilings stay 30 + 30 + 50 (existing marks such as Final 48.1/50 need them). The course total is that sum scaled: (CA + Mid + Final) × 100 ÷ 110, so it can never exceed 100, and the grade is read from it. The raw sum is still returned as `rawTotal`.
9. *What did you do with the duplicate sections?* For each group we kept the section with the most history and marked the others DROPPED. No deletes, so the FKs and published results stay intact. New duplicates get a 409 from the service.
10. *How is an exam clash defined?* Same date and `newStart < existingEnd AND existingStart < newEnd`. The strict inequalities allow back-to-back exams.
11. *How did you fix results published before their exam?* Re-timetabled the exams to unique past slots (published dates were not touched). The service now refuses COMPLETED or results before the exam's end time.
12. *Why NUMBER(2)?* NUMBER(1) holds −9..9, so semester 10 would raise ORA-01438 despite CHECK 1..10.
13. *Where are the status vocabularies enforced?* In Oracle (5 named CHECKs) and in the service, which only ever writes those values.

**JDBC, pool and HTTP**

14. *Why PreparedStatement?* Precompiled with bound `?` parameters, so there is no SQL injection, plus plan reuse. `JdbcTemplate` creates one per call.
15. *What does HikariCP do?* Keeps reusable physical connections (max 10), so a request borrows instead of opening a new TCP/Oracle session; it also handles timeouts and leak detection.
16. *How do transactions work?* `@Transactional` binds one connection for the service method; a RuntimeException rolls back the checks and the writes together.
17. *Why 409 vs 400 vs 404 vs 403?* 400 means the input itself is invalid; 404 means the target id doesn't exist; 409 means valid input that conflicts with current state (duplicate, capacity, clash, FK children); 403 means authenticated but not allowed; 401 means not authenticated.
18. *How do you stop a student reading another student's data?* `AuthInterceptor` compares the `{id}` in `/api/student/{id}` with the session's id and returns 403. Faculty are also checked against offering assignment in the service.
19. *Why don't the tests pollute the database?* JUnit tests run inside a transaction that is rolled back. The PowerShell suite creates its own fixtures and deletes them through the API, and the before/after snapshots match.
20. *How are passwords stored?* BCrypt hashes (salted, adaptive cost); hashes are never returned by any endpoint.
