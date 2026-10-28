# Student Academic & Examination Management System
### (Smart Campus Management System — XYZ University Academic Services Portal)

**Final Project Report · BACSE202 Database Systems · Fall Semester 2026**
Course Project (CO5, 30 marks) · Phase 1 (Assessment 8) → Phase 2 (Assessment 9) → Phase 3 (Assessment 10)
Vellore Institute of Technology · SCOPE

| Team member | Register No. | Primary role (per course brief) |
|---|---|---|
| Pratham Garg | 25BCE2016 | Database Designer — ER/EER, normalisation, DDL |
| S Rohith | 25BCE0790 | Front-End & Business Logic Developer — UI, workflow rules, validation |
| Divyam Kurkan | 25BCE2041 | Integration, Testing & Documentation Lead — JDBC, test cases, report |

> **How to use this document.** Part A is the complete project report, organised by the course's 11 execution stages, so each heading maps to a rubric line. Part B gives slide-by-slide content for the final presentation, with visuals and speaker notes. Part C lists every source file, script and evidence file it refers to. All figures were taken from the live system on 28–29 Oct 2026.

---

## Table of contents

**Part A — Project report**
1. Abstract
2. Problem identification & requirement analysis *(Stage 1)*
3. ER / EER diagram *(Stage 2)*
4. ER to relational model *(Stage 3)*
5. Normalisation 1NF → 2NF → 3NF *(Stage 4)*
6. Database design & table creation — DDL *(Stage 5)*
7. Data population — DML *(Stage 6)*
8. Front-end design *(Stage 7)*
9. Business logic implementation *(Stage 8)*
10. Database connectivity — JDBC, complete CRUD *(Stage 9)*
11. Review 2 audit findings and how they were closed
12. Testing & validation *(Stage 10)*
13. Demonstration, screenshots & documentation *(Stage 11)*
14. Security
15. How to run
16. Limitations & future work
17. Conclusion
18. References

**Part B — Final presentation content** (15 slides + backup)

**Part C — Artefact index**

---

# PART A — PROJECT REPORT

## 1. Abstract

The project replaces scattered academic records with a single relational database and a role-based web portal, modelled on the Academic and Examination modules of VIT's VTOP. Students register for courses and exams, and view attendance, marks, grades and results. Faculty manage their own offerings: classes held, attendance and marks. Administrators manage the curriculum, offerings, exam timetables, result publication, re-examination decisions and user accounts.

The design started from **12 functional requirements**, an ER model of **14 entities and 18 relationships**, and was normalised to **3NF**. It is implemented in **Oracle AI Database 26ai Free** as **15 tables with 20 foreign keys and 11 named CHECK constraints**, seeded with **1,712 rows**.

The application is written in **Java 21 / Spring Boot 3.3.4**. It reaches Oracle only through **JDBC** (`JdbcTemplate` → PreparedStatement) with a **HikariCP** pool and serves a vanilla HTML/CSS/JavaScript single-page front end. It exposes 69 REST endpoints, and every table supports create, read, update and delete (or logical retirement) end to end.

Verification:
- Oracle constraint probes: **23/23**
- SQL data audits: **0 violations**
- JUnit/MockMvc integration tests against the live database (rolled back): **36/36**
- HTTP workflow suite: **46/46**, with the live data unchanged afterwards
- **23 screenshots**

## 2. Problem identification & requirement analysis *(Stage 1)*

### 2.1 Problem
University students need one place to access courses, attendance, marks, grades, exam schedules and exam registrations. These are many tightly related records used by several kinds of users. Without a structured, constraint-checked database they drift out of sync: duplicate registrations, attendance above classes held, grades that do not match marks, and results published before an exam.

### 2.2 Proposed solution
A centralised, database-driven portal with two sections, **Academics** and **Examinations**, and three roles. Every screen reads from and writes to one normalised Oracle schema, and the browser never touches the database directly.

### 2.3 Users and scope

| Role | Can do |
|---|---|
| **Student** | Dashboard/profile, course registration (live seats), drop a course, attendance %, marks and grade history, exam schedule, exam registration and cancellation, published results, re-exam application and withdrawal |
| **Faculty** | Own offerings only: class roster, classes held, attendance entry, marks entry, invigilation schedule |
| **Administrator** | Departments, programs, courses, offerings, students, faculty, exams (schedule / complete / cancel), result publication, re-exam decisions, user accounts, live KPI dashboard |

**Out of scope (agreed in Review 1):** hostel, finance, admissions, placement/PAT, events, transport, feedback, alumni, parent portal.

### 2.4 Functional requirements

| ID | User | Requirement | Where it is implemented |
|---|---|---|---|
| FR1 | Admin/Faculty | Maintain student information | Admin › Student Directory (create / edit / delete) |
| FR2 | Admin/Faculty | Maintain course and faculty information | Admin › Course Catalog, Faculty Directory |
| FR3 | Student | Register for available courses | Student › Course Offerings & Registration |
| FR4 | Admin/Faculty | Maintain attendance records | Faculty › roster › Attendance; Classes Held |
| FR5 | Admin/Faculty | Maintain marks | Faculty › roster › Marks |
| FR6 | Student/System | Provide grades and grade history | Student › Continuous Assessment & Marks; Grade Sheet |
| FR7 | Admin | Create and publish exam schedules | Admin › Schedule Examinations |
| FR8 | Student | Register for examinations | Student › Exam Schedules & Registration |
| FR9 | Student/System | Provide examination results | Student › Published Results; Admin › Results publication |
| FR10 | Student | Submit re-examination applications | Student › Published Results › Apply; Re-Exam Applications |
| FR11 | Faculty | Enter and update attendance and marks | Faculty portal (own offerings only) |
| FR12 | Admin | Manage courses, faculty, students and exams | Admin portal (all entities) |

### 2.5 Non-functional requirements
* **Integrity:** PK/FK/UNIQUE/CHECK in Oracle, with business rules in the service layer.
* **Security:** BCrypt passwords, session authentication, role isolation (401/403), parameterised SQL only.
* **Usability:** clear messages for every error (400/403/404/409), no hardcoded data, responsive tables with search.
* **Maintainability:** layered architecture, idempotent SQL scripts, automated tests.

## 3. ER / EER diagram *(Stage 2)*

Drawn in draw.io (`VTOP_ER_Diagram_Final.drawio`) in Chen notation: **14 entities and 18 relationships**.

**Entities:** DEPARTMENT, PROGRAM, STUDENT, FACULTY, COURSE, COURSE_OFFERING, ENROLLMENT, ATTENDANCE, MARKS, GRADE, EXAM, EXAM_REGISTRATION, EXAM_RESULT, RE_EXAM_APPLICATION.

**Relationships (one side → many side):**

| Kind | Relationships |
|---|---|
| **1 : N (17)** | OFFERS (DEPARTMENT→PROGRAM), EMPLOYS (DEPARTMENT→FACULTY), MANAGES (DEPARTMENT→COURSE), ADMITS (PROGRAM→STUDENT), OFFERED AS (COURSE→COURSE_OFFERING), TEACHES (FACULTY→COURSE_OFFERING), MAKES (STUDENT→ENROLLMENT), RECEIVES (COURSE_OFFERING→ENROLLMENT), HAS (STUDENT→ATTENDANCE), RECORDS (COURSE_OFFERING→ATTENDANCE), EARNS (STUDENT→MARKS), ASSESSES (COURSE_OFFERING→MARKS), SCHEDULES (COURSE_OFFERING→EXAM), SUBMITS (STUDENT→EXAM_REGISTRATION), REGISTERED VIA (EXAM→EXAM_REGISTRATION), AWARDED IN (GRADE→EXAM_RESULT), CONTESTED BY (EXAM_RESULT→RE_EXAM_APPLICATION) |
| **1 : 1 (1)** | PRODUCES (EXAM_REGISTRATION→EXAM_RESULT), mapped as an FK with UNIQUE |
| **M : N resolved (2 pairs)** | STUDENT↔COURSE_OFFERING through ENROLLMENT/ATTENDANCE/MARKS; STUDENT↔EXAM through EXAM_REGISTRATION |

**Design notes**
* DEPARTMENT is the hub: it offers programs, employs faculty and manages courses.
* COURSE is the subject; COURSE_OFFERING is one semester/section of it.
* AWARDED IN is partial on EXAM_RESULT, because a withheld result has no grade yet.
* Derived values (attendance %, totals) are never stored.

*Insert: full ER diagram and the three module views from the Review 1 deck (slides 5–8).*

## 4. ER to relational model *(Stage 3)*

Four mapping rules:
1. **Entity → table.** Each key attribute becomes the primary key.
2. **1 : N → foreign key on the N side.** For example, `STUDENT.Program_ID → PROGRAM`.
3. **1 : 1 → foreign key + UNIQUE** on the total-participation side: `EXAM_RESULT.Exam_Registration_ID UNIQUE`.
4. **M : N → associative table** with both FKs and UNIQUE on the pair: `ENROLLMENT (Student_ID, Offering_ID)`.

No attribute is multivalued or composite, so no extra attribute tables are needed.

| # | Table | PK | Foreign keys | UNIQUE | Other attributes |
|---|---|---|---|---|---|
| 1 | DEPARTMENT | Department_ID | — | Department_Name | Office_Location |
| 2 | PROGRAM | Program_ID | Department_ID | Program_Name | Degree, Duration_Years |
| 3 | STUDENT | Student_ID | Program_ID | Email | Name, Phone, DOB, Admission_Year |
| 4 | FACULTY | Faculty_ID | Department_ID | Email | Name, Phone, Designation |
| 5 | COURSE | Course_ID | Department_ID | — | Course_Name, Credits, Course_Type |
| 6 | COURSE_OFFERING | Offering_ID | Course_ID, Faculty_ID | (Course_ID, Semester, Academic_Year, Section) | Semester, Academic_Year, Section, Room_No, Max_Seats, Classes_Held |
| 7 | ENROLLMENT | Enrollment_ID | Student_ID, Offering_ID | (Student_ID, Offering_ID) | Enrollment_Date, Status |
| 8 | ATTENDANCE | Attendance_ID | Student_ID, Offering_ID | (Student_ID, Offering_ID) | Classes_Attended |
| 9 | MARKS | Mark_ID | Student_ID, Offering_ID | (Student_ID, Offering_ID) | Continuous_Assessment_Marks, Midterm_Marks, Final_Exam_Marks |
| 10 | GRADE | Grade_Code | — | — | Grade_Point, Min_Percentage, Max_Percentage |
| 11 | EXAM | Exam_ID | Offering_ID | — | Exam_Type, Exam_Date, Start_Time, End_Time, Venue, Max_Marks, Status |
| 12 | EXAM_REGISTRATION | Exam_Registration_ID | Student_ID, Exam_ID | (Student_ID, Exam_ID) | Registration_Date, Status |
| 13 | EXAM_RESULT | Exam_Result_ID | Exam_Registration_ID, Grade_Code | Exam_Registration_ID | Marks_Obtained, Result_Status, Published_Date |
| 14 | RE_EXAM_APPLICATION | ReExam_Application_ID | Exam_Result_ID | — | Application_Date, Reason, Status, Approval_Date |
| 15 | USER_ACCOUNT *(added in Phase 2 for login)* | Account_ID (identity) | Student_ID, Faculty_ID | Username | Password_Hash (BCrypt), Role, Is_Active, Created_At, Last_Login |

**Totals:** 14 academic tables and 18 FKs, plus USER_ACCOUNT with 2 FKs, giving **15 tables and 20 FKs** (verified in `USER_CONSTRAINTS`).

## 5. Normalisation 1NF → 2NF → 3NF *(Stage 4)*

### 5.1 Functional dependencies

| FD | Dependency |
|---|---|
| FD1 | Student_ID → Name, Email, Phone, DOB, Admission_Year, Program_ID |
| FD2 | Program_ID → Program_Name, Degree, Duration_Years, Department_ID |
| FD3 | Department_ID → Department_Name, Office_Location |
| FD4 | Course_ID → Course_Name, Credits, Course_Type, Department_ID |
| FD5 | Faculty_ID → Name, Email, Phone, Designation, Department_ID |
| FD6 | Offering_ID → Course_ID, Faculty_ID, Semester, Academic_Year, Section, Room_No, Max_Seats, **Classes_Held** |
| FD7 | {Student_ID, Offering_ID} → Enrollment_Date, Status, Classes_Attended, CA, Midterm, Final marks |
| FD8 | Exam_ID → Offering_ID, Exam_Type, Exam_Date, Start/End_Time, Venue, Max_Marks, Status |
| FD9 | {Student_ID, Exam_ID} → Registration_Date, Status, Marks_Obtained, Grade_Code, Result_Status, Published_Date |
| FD10 | Grade_Code → Grade_Point, Min_Percentage, Max_Percentage |
| FD11 | ReExam_Application_ID → Exam_Result_ID, Application_Date, Reason, Status, Approval_Date |

### 5.2 UNF → 1NF
`STUDENT_ACADEMIC_RECORD` held three nested repeating groups: G1 course offerings, G2 exams inside them, and G3 re-exam applications inside those. Each group moved to its own relation, carrying its parent's key:

| 1NF relation | Primary key |
|---|---|
| STUDENT_1 | Student_ID |
| STUDENT_OFFERING_1 | {Student_ID, Offering_ID} |
| STUDENT_EXAM_1 | {Student_ID, Exam_ID} |
| RE_EXAM_APPLICATION_1 | ReExam_Application_ID |

The groups were not flattened into one table, because a student enrolled before the first exam would then need a null in the primary key.

### 5.3 1NF → 2NF (remove partial dependencies)
* **STUDENT_OFFERING_1:** Offering_ID alone determines the offering, course and faculty details and Classes_Held (FD6), so they move to **OFFERING_2**. The full-key attributes stay in **ENROLLMENT_2** (FD7).
* **STUDENT_EXAM_1:** Exam_ID alone determines the exam details (FD8), so they move to **EXAM_2**. The full-key attributes stay in **EXAM_REG_2** (FD9).
* The splits are lossless: the shared attribute is the key of the new relation. The result is six relations.

### 5.4 2NF → 3NF (remove transitive dependencies)

| 2NF relation | Transitive dependency | Split into |
|---|---|---|
| STUDENT_1 | Student_ID → Program_ID → … → Department_ID → … | STUDENT, PROGRAM, DEPARTMENT |
| OFFERING_2 | Offering_ID → Course_ID → …; Offering_ID → Faculty_ID → … | COURSE_OFFERING, COURSE, FACULTY |
| EXAM_REG_2 | {Student_ID, Exam_ID} → Grade_Code → Grade_Point, ranges | EXAM_REGISTRATION_3, GRADE |
| ENROLLMENT_2, EXAM_2, RE_EXAM_APPLICATION_1 | none | unchanged |

This gives 11 relations in 3NF. The three department copies merge into one DEPARTMENT table. Splitting on the same key (ENROLLMENT → ENROLLMENT, ATTENDANCE, MARKS; EXAM_REGISTRATION → EXAM_REGISTRATION, EXAM_RESULT) gives the **14 final tables**, still lossless and in 3NF.

### 5.5 Two corrections made by normalisation
1. **Classes_Held moved to COURSE_OFFERING.** It depends on Offering_ID alone (FD6). Kept in ATTENDANCE, it was a partial dependency repeated for every student, and attendance % is now computed rather than stored.
2. **Student_ID removed from RE_EXAM_APPLICATION.** Exam_Result_ID → Exam_Registration_ID → Student_ID is a transitive dependency, so the student is reached through the result.

## 6. Database design & table creation — DDL *(Stage 5)*

**Script:** `sql/01_create_schema.sql` (14 tables); USER_ACCOUNT comes from `sql/04_authentication_migration.sql`.

### 6.1 Integrity constraints

| Kind | Constraints |
|---|---|
| Primary keys | one per table (USER_ACCOUNT uses an identity column) |
| Foreign keys | 20, e.g. `FK_ENROLLMENT_STUDENT`, `FK_RESULT_REG`, `FK_REEXAM_RESULT`, `FK_ACC_STUDENT` |
| UNIQUE | Department/Program name, Student/Faculty email, `UQ_OFFERING`, `UQ_ENROLLMENT`, `UQ_ATTENDANCE`, `UQ_MARKS`, `UQ_EXAMREG`, EXAM_RESULT.Exam_Registration_ID (1:1), Username |
| CHECK, original | Duration > 0, Credits > 0, Max_Seats > 0, Classes_Held ≥ 0, Classes_Attended ≥ 0, Grade_Point 0–10, % 0–100, `CK_GRADE_RANGE`, `CK_EXAM_TIME` (End > Start), Max_Marks > 0, Marks_Obtained ≥ 0, `CK_ACCOUNT_REF`, Role IN (…), Is_Active IN (0,1) |
| CHECK, Phase 3 | **Semester NUMBER(2) CHECK 1–10**; `CK_ENROLLMENT_STATUS` (ENROLLED/DROPPED/COMPLETED); `CK_EXAM_STATUS` (SCHEDULED/COMPLETED/CANCELLED); `CK_EXAMREG_STATUS` (REGISTERED/CANCELLED); `CK_RESULT_STATUS` (PASS/FAIL/WITHHELD); `CK_REEXAM_STATUS` (PENDING/APPROVED/REJECTED); `CK_MARKS_CA` 0–30; `CK_MARKS_MIDTERM` 0–30; `CK_MARKS_FINAL` 0–50 |

**Example (`ENROLLMENT`):**
```sql
CREATE TABLE ENROLLMENT (
  Enrollment_ID   NUMBER PRIMARY KEY,
  Student_ID      NUMBER NOT NULL,
  Offering_ID     NUMBER NOT NULL,
  Enrollment_Date DATE DEFAULT SYSDATE NOT NULL,
  Status VARCHAR2(20) DEFAULT 'ENROLLED' NOT NULL
         CONSTRAINT CK_ENROLLMENT_STATUS CHECK (Status IN ('ENROLLED','DROPPED','COMPLETED')),
  CONSTRAINT FK_ENROLLMENT_STUDENT  FOREIGN KEY (Student_ID)  REFERENCES STUDENT(Student_ID),
  CONSTRAINT FK_ENROLLMENT_OFFERING FOREIGN KEY (Offering_ID) REFERENCES COURSE_OFFERING(Offering_ID),
  CONSTRAINT UQ_ENROLLMENT UNIQUE (Student_ID, Offering_ID));
```

### 6.2 Which rule is enforced where

| Rule | Oracle | Java service |
|---|---|---|
| No duplicate enrollment / exam registration | UNIQUE | 409 with message |
| One result per registration | UNIQUE FK | 409 |
| Valid status vocabulary, semester 1–10, marks 30/30/50 | CHECK | 400 (DTO validation) |
| Offering not over capacity | — (needs COUNT) | 409 |
| Attended ≤ Classes_Held | — (spans 2 tables) | 400 |
| One section per course per term | — (spans 2 tables) | 409 |
| No overlapping exam registrations | — (time overlap) | 409 |
| No results before an exam has ended | — (needs SYSDATE) | 409 |
| Parent delete with children | FK (ORA-02292) | pre-check → 409 with counts |

## 7. Data population — DML *(Stage 6)*

**Scripts:** `sql/02_insert_sample_data.sql` (45 rows), `sql/03_expand_sample_data.sql` (1,591 rows, idempotent `INSERT … SELECT … WHERE NOT EXISTS`), `sql/04_authentication_migration.sql` (76 BCrypt logins).

| Table | Seeded | Live (28-Oct-2026) |
|---|---:|---:|
| DEPARTMENT | 6 | 7 |
| PROGRAM | 10 | 10 |
| STUDENT | 60 | 60 |
| FACULTY | 15 | 15 |
| COURSE | 30 | 30 |
| COURSE_OFFERING | 45 | 45 |
| ENROLLMENT | 365 | 372 (314 ENROLLED, 58 DROPPED) |
| ATTENDANCE | 365 | 372 |
| MARKS | 250 | 257 |
| GRADE | 6 | 6 |
| EXAM | 42 | 42 |
| EXAM_REGISTRATION | 232 | 233 |
| EXAM_RESULT | 190 | 190 |
| RE_EXAM_APPLICATION | 20 | 21 (7 pending) |
| USER_ACCOUNT | 76 | 76 |
| **Total** | **1,712** | **1,736** |

The 24 extra live rows were created through the portal. The "1,896" quoted earlier was the **line** count of `dataset_export.txt`, not a row count.

**Insertion order follows FK dependencies:** L0 DEPARTMENT, GRADE → L1 PROGRAM, FACULTY, COURSE → L2 STUDENT, COURSE_OFFERING → L3 ENROLLMENT, ATTENDANCE, MARKS, EXAM, USER_ACCOUNT → L4 EXAM_REGISTRATION → L5 EXAM_RESULT → L6 RE_EXAM_APPLICATION.

**Grade scale (GRADE):** S 90–100 (10), A 80–89.99 (9), B 70–79.99 (8), C 60–69.99 (7), D 50–59.99 (6), F 0–49.99 (0).

**Representative queries used by the application**
```sql
-- Student's enrollments (4-table join rebuilds what 3NF split apart)
SELECT e.Enrollment_ID, c.Course_Name, c.Credits, co.Section, f.Name, e.Enrollment_Date, e.Status
FROM ENROLLMENT e
JOIN COURSE_OFFERING co ON e.Offering_ID = co.Offering_ID
JOIN COURSE c  ON co.Course_ID  = c.Course_ID
JOIN FACULTY f ON co.Faculty_ID = f.Faculty_ID
WHERE e.Student_ID = ?;

-- Exam clash check (Phase 3): an overlapping exam already registered by this student
SELECT other.Exam_ID FROM EXAM target
JOIN EXAM other ON other.Exam_Date = target.Exam_Date AND other.Exam_ID <> target.Exam_ID
     AND target.Start_Time < other.End_Time AND other.Start_Time < target.End_Time
JOIN EXAM_REGISTRATION er ON er.Exam_ID = other.Exam_ID AND er.Student_ID = ? AND er.Status = 'REGISTERED'
WHERE target.Exam_ID = ?;

-- Faculty updates attendance
UPDATE ATTENDANCE SET Classes_Attended = ? WHERE Student_ID = ? AND Offering_ID = ?;
```

## 8. Front-end design *(Stage 7)*

**Technology:** HTML5, CSS3 and a vanilla JavaScript single-page application (`index.html`, `css/portal.css`, `js/api.js`, `app.js`, `student.js`, `faculty.js`, `admin.js`, `admin-crud.js`, about 3,700 lines). It uses the Fetch API with a session cookie. There is no framework and no hardcoded data: every table is rendered from API responses.

**Layout:** a role-selection landing page leads to a role sign-in. The portal has a top bar showing the live database status (`Oracle 26ai: Connected`), the user and sign-out. Each portal has two sections, **Academics** and **Examinations**, each with a sub-navigation.

| Portal | Screens |
|---|---|
| Student | Academic Profile · Course Offerings & Registration (live `enrolled / Max_Seats`, search, Register / Drop) · Attendance Record (% computed, shortage warning below 75 %) · Continuous Assessment & Marks (`/30 /30 /50`, total `/100`, grade, grade point, GPA, Drop) · Official Grade Sheet (printable) · Exam Schedules & Registration (Register / Cancel) · Published Results & Grades (Apply for re-exam) · Re-Examination Applications (status, Withdraw) |
| Faculty | Assigned Course Offerings (Edit Classes Held) · class roster with attendance %, marks and total `/100` · Attendance and Marks entry modals · Invigilation duties |
| Administrator | Academic Overview (8 live KPI tiles) · Departments · Degree Programs · Course Catalog · Course Offerings · Student Directory · Faculty Directory · Account Provisioning · User Accounts · Schedule Examinations (Results / Mark Completed / Cancel / Edit / Delete) · Published Results · Re-Exam Decisions |

**UX details:**
- every write shows a toast: success, or `[status] message` on error;
- buttons lock while a request is in flight, which prevents double submission;
- confirmation dialogs appear before destructive actions;
- tables scroll horizontally and have live search filters;
- server messages are rendered as text, never as HTML;
- static files are served with `Cache-Control: no-cache` plus versioned URLs, so a new build is always picked up.

## 9. Business logic implementation *(Stage 8)*

All rules live in the service layer (`StudentService`, `FacultyService`, `AdminService`, `AuthService`), inside `@Transactional` methods, so checks and writes commit or roll back together.

| Area | Rules (HTTP result on violation) |
|---|---|
| Course registration | offering must exist (404); not already enrolled (409); **no other section of the same course in the same semester and year (409)**; seats available (409); a dropped row is re-activated instead of duplicated; attendance and marks rows are created automatically |
| Drop course | must be actively enrolled (404); not allowed once results are published (409); status becomes `DROPPED` and pending exam registrations for that offering are `CANCELLED`; attendance and marks history is kept |
| Exam registration | exam must exist (404); not already registered (409); must be enrolled in the offering (409); exam must be `SCHEDULED` and not yet started (409); **no time overlap with another registered exam on the same date (409)**, while back-to-back exams are allowed |
| Exam cancellation | only before the exam starts and if no result exists |
| Attendance | faculty must own the offering (403); student must be enrolled (404); 0 ≤ attended ≤ Classes_Held (400); Classes_Held cannot drop below an attended value already recorded (400) |
| Marks | faculty must own the offering (403); CA 0–30, Midterm 0–30, Final 0–50 (400); **course total = (CA + Mid + Final) × 100 ÷ 110, so it never exceeds 100**; grade from GRADE using that total |
| Exam status | `COMPLETED` only after the exam's end time (409); an exam with results cannot be re-opened or cancelled (409) |
| Results | exam must have ended and be `COMPLETED` (409, so no results for future exams); 0 ≤ marks ≤ Max_Marks (400); grade and PASS/FAIL derived from GRADE (marks ÷ Max_Marks × 100); `WITHHELD` hidden from students; one per registration (409); cannot delete if a re-exam application exists (409) |
| Re-exam | only on one's own (403), non-withheld result; one per result (409); reason of at least 5 characters (400); admin decides `PENDING → APPROVED/REJECTED` once (409 afterwards), setting Approval_Date; student may withdraw only `PENDING` |
| Admin CRUD | parent records cannot be deleted while children exist (409, lists the counts); offering seats cannot drop below the enrolled count; a unique (course, semester, year, section) is required; the last active administrator is protected |
| Accounts | BCrypt hashing; provisioning requires an existing student/faculty record (404) and no existing account (409); deactivated accounts cannot sign in |

## 10. Database connectivity — JDBC, complete CRUD *(Stage 9)*

### 10.1 Architecture
```
Browser (SPA) ──fetch + JSESSIONID──▶ AuthInterceptor (401 / 403)
   ▶ REST Controllers (Auth, Student, Faculty, Admin) + GlobalExceptionHandler
   ▶ Services (business rules, @Transactional)
   ▶ JDBC Repositories (JdbcTemplate → PreparedStatement, '?' binds only)
   ▶ HikariCP pool (max 10, min idle 2)
   ▶ Oracle 26ai Free · FREEPDB1 · Docker container oracle-db :1521
```
The database credentials stay on the server, read from the `CAMPUS_DB_PASSWORD` environment variable. The browser has no direct path to Oracle.

### 10.2 JDBC usage
* `spring-boot-starter-jdbc` provides `JdbcTemplate` and HikariCP; the driver is `ojdbc11`.
* Each repository call prepares a statement, binds `?` parameters, maps the `ResultSet` with a lambda and returns the pooled connection. SQL text is never built from user input, which rules out SQL injection.
* `@Transactional` binds one connection per service call: a business-rule failure rolls back every statement in it.
* Oracle errors are translated: ORA-00001 → 409 duplicate; ORA-02291/02292 → 409 parent missing / children exist; ORA-02290 → 409 CHECK violated. Messages never contain SQL text.

### 10.3 CRUD coverage (all 15 tables)

| Table | Create | Read | Update | Delete / retire |
|---|---|---|---|---|
| DEPARTMENT, PROGRAM, COURSE, FACULTY, STUDENT | Admin POST | lists, profiles | Admin PUT | Admin DELETE (409 if referenced) |
| COURSE_OFFERING | Admin POST | admin / student / faculty views | Admin PUT; faculty Classes_Held | Admin DELETE (409 if used) |
| ENROLLMENT | Student enroll | student, roster | re-activate | Student drop → `DROPPED` |
| ATTENDANCE / MARKS | auto on enroll | student, roster | Faculty PUT | kept as history |
| GRADE | seeded reference data | used for every grade | via migration | — |
| EXAM | Admin POST | all portals | Admin PUT; PATCH status | Admin DELETE (409 if registrations) or `CANCELLED` |
| EXAM_REGISTRATION | Student register | student, admin | re-activate | Student cancel → `CANCELLED` |
| EXAM_RESULT | Admin publish | student, admin | Admin PUT (re-graded) | Admin DELETE (409 if re-exam) |
| RE_EXAM_APPLICATION | Student apply | student, admin | Admin decision | Student withdraw (PENDING) |
| USER_ACCOUNT | Admin provision | Admin list (no hashes) | activate / deactivate, reset password | Admin DELETE |

There are **69 REST endpoints** in total. Creates return 201, other successes 200; errors return 400, 401, 403, 404 or 409. The per-endpoint matrix is in `docs/phase3/CRUD_COVERAGE.md`.

## 11. Review 2 audit findings and how they were closed

| # | Finding (Review 2, Appendix A) | Fix | Before → after |
|---|---|---|---|
| 1 | "1,896 records" vs 1,712 | Documented: 1,896 is a line count; audit script `01_audit_row_counts.sql` | — |
| 2 | Marks total shown "/100" though ceilings sum to 110 | Components shown with ceilings; total scaled to /100 (sum × 100 ÷ 110); grade on that total; Oracle CHECKs 30/30/50 | 0 component violations |
| 3 | 3 seed grades off-scale | 10002 → B, 10003 → A, 10004 → D (seed and live) | 3 → 0 |
| 4 | Same course in two sections | Service rule (409); 58 duplicate rows set to `DROPPED`, none deleted | 51 groups → 0 |
| 5 | Clashing exam registrations | Overlap rule (409); 42 seed exams moved into unique, non-overlapping slots | 102 pairs → 0 |
| 6 | Results before exam date; future exams COMPLETED | Service blocks completion and publication before the exam ends; timetable reconciled | 115 + 24 + 3 → 0 |
| 7 | Semester NUMBER(1) cannot store 10 | `ALTER … MODIFY (Semester NUMBER(2))` | fixed |
| 8 | Five unchecked status columns | Five named CHECK constraints | 5 → 0 missing |

The live database was migrated with `database/phase3/02_migrate_schema_and_data.sql` (idempotent: a second run changes nothing). Fresh installs get the same state from `sql/01–04`. Reconciliation decisions are recorded in `docs/phase3/KNOWN_ISSUES.md`.

## 12. Testing & validation *(Stage 10)*

### 12.1 Strategy (four layers)

| Layer | Tool | What it proves | Result |
|---|---|---|---|
| Constraint probes | PL/SQL, every probe rolled back (`03_verify_post_migration.sql`) | Oracle itself rejects bad data | **23 / 23 pass** |
| Data audit | SQL (`01_audit_all.sql`) | Live data satisfies every rule | **0 violations** (13 checks) |
| Integration tests | JUnit 5 + MockMvc against the real Oracle schema, each test rolled back | Controllers → services → JDBC → Oracle behave as specified | **36 / 36 pass**, 0 rows left behind |
| End-to-end | PowerShell over HTTP (`verify_workflows.ps1`) | Real logins, real sessions, full workflows | **46 / 46 pass**; data snapshot identical before and after |

### 12.2 Representative test cases

| # | Scenario | Input | Expected | Actual |
|---|---|---|---|---|
| T1 | Valid course registration | free seat, new course | 201, row created | Pass |
| T2 | Duplicate registration | same offering again | 409 "already enrolled" | Pass |
| T3 | Other section, same course and term | Sec B after Sec A | 409 "another section" | Pass |
| T4 | Full offering | seats = enrolled | 409 "full capacity" | Pass |
| T5 | Unknown offering | id 99999999 | 404 | Pass |
| T6 | Drop course | active enrollment | 200, status DROPPED, history kept | Pass |
| T7 | Exam registration | scheduled future exam | 201 | Pass |
| T8 | Overlapping exam | 10:00–11:30 vs 09:00–10:30 | 409 "clash" | Pass |
| T9 | Back-to-back exam | 10:30 start after 10:30 end | 201 | Pass |
| T10 | Attendance above held | 11 of 10 | 400 | Pass |
| T11 | Negative attendance | −1 | 400 | Pass |
| T12 | Wrong faculty | other faculty's offering | 403 | Pass |
| T13 | Marks at ceilings | 30 / 30 / 50 | 200, total 100.00 / 100, grade S | Pass |
| T14 | Marks above ceiling | CA 31 / Final 51 / −1 | 400 | Pass |
| T15 | Raw sum above 100 | 28 + 27 + 47 = 102 | total 92.73 / 100 | Pass |
| T16 | Result above Max_Marks | 60 / 50 | 400 | Pass |
| T17 | Result for future exam | exam in 2099 | 409 "future exam" | Pass |
| T18 | Mark future exam COMPLETED | exam in 2099 | 409 | Pass |
| T19 | Re-exam lifecycle | apply → approve → decide again | 201 → 200 → 409 | Pass |
| T20 | Re-exam on another student's result | | 403 | Pass |
| T21 | Semester 1 / 10 / 0 / 11 | | 201 / 201 / 400 / 400 | Pass |
| T22 | Invalid status values in Oracle | 'BOGUS', 'DONE', … | ORA-02290 | Pass |
| T23 | Delete department with children | | 409 with counts | Pass |
| T24 | Delete unknown id | | 404 | Pass |
| T25 | No session | | 401 | Pass |
| T26 | Student reads another student / calls admin API | | 403 | Pass |
| T27 | Duplicate unique value | department name | 409, no "ORA-" in message | Pass |

The full lists are in `docs/phase3/TEST_PLAN_AND_RESULTS.md`. Raw outputs are in `docs/phase3/test-evidence/`.

## 13. Demonstration, screenshots & documentation *(Stage 11)*

### 13.1 Screenshots (`docs/phase3/screenshots/`)

| # | File | Shows |
|---|---|---|
| 1 | `01-login-role-selection.png` | Role selection landing page |
| 2 | `02-student-dashboard-profile.png` | Student profile |
| 3 | `03-student-course-registration-seats.png` | Offerings with live seats, Register / Drop |
| 4 | `04-student-attendance-percentage.png` | Attendance % (computed) |
| 5 | `05-student-marks-grade-history.png` | Marks /30 /30 /50, total /100, grades, Drop |
| 6 | `06-student-exam-schedule-registration.png` | Exam timetable and registration |
| 7 | `07-student-published-results.png` | Published results with grades |
| 8 | `08-student-reexam-applications.png` | Re-exam applications and status |
| 9 | `09-toast-409-cross-section-enrollment.png` | 409 for the other-section rule |
| 10 | `10-toast-403-student-calls-admin-api.png` | 403 role isolation |
| 11 | `11-faculty-assigned-offerings.png` | Faculty offerings |
| 12 | `12-faculty-roster-attendance-marks.png` | Roster with attendance % and totals |
| 13 | `13-faculty-marks-entry-modal.png` | Marks entry form |
| 14 | `14-toast-400-marks-above-ceiling.png` | 400 validation |
| 15 | `15-admin-dashboard-live-kpis.png` | Live KPI dashboard |
| 16 | `16-admin-offerings-crud-actions.png` | CRUD actions on offerings |
| 17 | `17-admin-edit-offering-modal.png` | Edit form |
| 18 | `18-toast-409-delete-department-with-children.png` | FK-protected delete |
| 19 | `19-admin-exam-timetable-actions.png` | Exam actions |
| 20 | `20-admin-result-publication-modal.png` | Result publication with auto-grade |
| 21 | `21-toast-409-complete-future-exam.png` | Future exam cannot be completed |
| 22 | `22-admin-reexam-decisions.png` | Re-exam decisions |
| 23 | `23-admin-user-accounts.png` | User account management |

Regenerate with `node scripts/capture_screenshots.js`.

### 13.2 Live demo (about 5 minutes)
1. **Student 1001:** attendance % → marks (total /100) → Register on #404 → 409 "another section" toast; in SQL*Plus, show that no row was added.
2. **Faculty 201:** roster for #401 → enter attendance 31 when held is 30 → 400.
3. **Admin:** KPI tiles → delete *Computer Science* → 409 with counts → Mark Completed on a November FAT → 409 → Results on exam #801 → re-grade → restore.
4. **SQL*Plus:** `@01_audit_all.sql` → every violation row reads 0.

The full script and a 20-question viva bank are in `docs/phase3/DEMO_AND_VIVA_GUIDE.md`.

## 14. Security
* Passwords are stored only as **BCrypt** hashes and never returned by any API.
* Session-based login; `AuthInterceptor` enforces the role per URL space and same-user access on `/api/student/{id}` and `/api/faculty/{id}`; faculty may only touch offerings assigned to them.
* All SQL is parameterised; errors never echo SQL; toasts render text, not HTML.
* No secrets in the repository: the database password comes from `CAMPUS_DB_PASSWORD` (scripts prompt if it is missing), and the git history was rewritten to remove an earlier hardcoded value.
* The last active administrator cannot be deactivated or deleted.

## 15. How to run
1. Start Docker Desktop (container `oracle-db`, Oracle 26ai Free, service `FREEPDB1`).
2. Set the password once: `setx CAMPUS_DB_PASSWORD "<password>"`.
3. Fresh database: run `sql/01` → `02` → `03` → `04`. Existing database: run `database/phase3/02_migrate_schema_and_data.sql` (see `docs/phase3/MIGRATION_GUIDE.md`).
4. Run `run.bat` (or `run.ps1`) and open <http://localhost:8080>.
5. Run the tests: `cd SmartCampus` then `mvn test`, and `powershell -File scripts\verify_workflows.ps1`.

## 16. Limitations & future work
* The cross-section rule is enforced in Java; a compound trigger could also enforce it in Oracle.
* IDs use `MAX(id) + 1`; Oracle sequences or identity columns would remove the rare concurrent-insert collision.
* CGPA across semesters, attendance-shortage reports, an audit log of admin changes, email notifications.
* Production hardening: HTTPS, CSRF tokens, per-user passwords instead of the shared dev password.
* Reconciliation decisions kept for history (58 DROPPED rows, re-timetabled seed exams) are listed in `docs/phase3/KNOWN_ISSUES.md`.

## 17. Conclusion
The project went from 12 requirements and a 14-entity ER model, through a lossless 3NF decomposition, to a 15-table Oracle schema with 20 foreign keys and 11 named CHECK constraints. On top of it runs a three-role web portal that performs all CRUD operations through JDBC. Every Review 2 audit finding is closed in the code, the seed scripts and the live database. Each claim in this report is backed by an evidence file produced by automated tests: 23 constraint probes, a zero-violation data audit, 36 integration tests and 46 end-to-end scenarios.

## 18. References
1. R. Elmasri, S. B. Navathe, *Fundamentals of Database Systems*, 7th ed., Pearson.
2. Oracle, *Oracle AI Database 26ai SQL Language Reference*.
3. Spring, *Spring Framework Reference: Data Access with JDBC*, and *Spring Boot Reference Documentation 3.3*.
4. HikariCP project documentation.
5. BACSE202 Course Project Overview (VIT SCOPE), Review 1 and Review 2 decks of this team.

---

# PART B — FINAL PRESENTATION CONTENT

*15 main slides plus 3 backup slides, about 12–15 minutes with the demo. "Visual" names the image to place; "Say" is the speaker note. Suggested split: Pratham 1–5 & 15, Rohith 6–10, Divyam 11–14.*

**Slide 1 — Title**
- Student Academic & Examination Management System (Smart Campus · XYZ University)
- BACSE202 · Phase 3: Integration & Delivery · Assessment 10
- Pratham Garg 25BCE2016 · S Rohith 25BCE0790 · Divyam Kurkan 25BCE2041
- Visual: `01-login-role-selection.png`
- Say: "A VTOP-inspired academic and exam portal, built end-to-end on Oracle through JDBC."

**Slide 2 — Problem & solution**
- Many tightly related records (courses, attendance, marks, exams, results, re-exams) used by three kinds of user
- One normalised Oracle schema, one portal, role-specific access
- Scope: Academics + Examinations; out of scope: hostel, finance, placement …
- Say: "The core problem is consistency across related records."

**Slide 3 — Requirements**
- 12 functional requirements: 5 student-facing, 7 admin/faculty (table from §2.4, condensed)
- Three roles and what each can do
- Say: "Every requirement maps to a screen we'll show."

**Slide 4 — ER model**
- 14 entities · 18 relationships (17 × 1:N, 1 × 1:1, 2 M:N pairs resolved)
- Visual: full ER diagram (Review 1 slide 5)
- Say: "DEPARTMENT is the hub; COURSE vs COURSE_OFFERING separates subject from section."

**Slide 5 — Normalisation**
- UNF → 4 (1NF) → 6 (2NF) → 11 (3NF) → 14 final tables
- Fixes: Classes_Held → COURSE_OFFERING; Student_ID removed from RE_EXAM_APPLICATION
- Visual: normalisation flow (Review 1 slide 18)
- Say: "Both fixes remove anomalies; attendance % is computed, never stored."

**Slide 6 — Architecture & JDBC**
- Browser → AuthInterceptor → Controllers → Services → JdbcTemplate (PreparedStatement) → HikariCP → Oracle 26ai
- 69 REST endpoints; credentials stay on the server
- Say: "No ORM: every SQL statement is visible and parameterised."

**Slide 7 — Schema in Oracle**
- 15 tables (14 + USER_ACCOUNT), 20 FKs, 11 named CHECKs
- New in Phase 3: Semester NUMBER(2) 1–10; 5 status CHECKs; marks 30/30/50
- Visual: relational schema (Review 1 slide 11) or a constraint table
- Say: "Oracle rejects bad data even if the application is bypassed: 23/23 probes."

**Slide 8 — Data**
- 1,712 seeded rows (1,636 academic + 76 logins); 1,736 live
- Parents-first insertion order L0–L6; idempotent scripts
- Say: "1,896 was a line count; the row count is 1,712."

**Slide 9 — Review 2 findings: all 8 closed**
- Table from §11 with before → after numbers (3 → 0, 51 → 0, 102 → 0, 115 + 24 + 3 → 0)
- Say: "Fixed in code, seed scripts and the live database, by an idempotent migration."

**Slide 10 — Business rules**
- Cross-section 409 · exam clash 409 (back-to-back allowed) · no results before an exam ends · capacity · attended ≤ held · ceilings
- Visuals: `09-toast-409-cross-section-enrollment.png`, `21-toast-409-complete-future-exam.png`
- Say: "Rules that span tables or time live in the service layer, inside one transaction."

**Slide 11 — Complete CRUD**
- CRUD matrix summary (§10.3); logical retirement (DROPPED / CANCELLED) vs FK-protected delete (409)
- Visuals: `16-admin-offerings-crud-actions.png`, `18-toast-409-delete-department-with-children.png`
- Say: "We never cascade-delete academic history."

**Slide 12 — Student portal**
- Registration with live seats and Drop; attendance %; marks with total /100; results; re-exam
- Visuals: `04-…`, `05-student-marks-grade-history.png`, `07-…`, `08-…`
- Say: "These are the four screens Review 2 asked us to capture."

**Slide 13 — Faculty & admin portals**
- Faculty roster and marks entry (own offerings only); admin KPIs, result publication with auto-grade, user accounts
- Visuals: `12-…`, `13-…`, `15-…`, `20-…`

**Slide 14 — Testing & validation**
- 4 layers: probes 23/23 · audit 0 violations · JUnit 36/36 (live Oracle, rolled back) · workflow 46/46 with data unchanged
- Visual: a summary table, or a cropped part of `verify_workflows_output.txt`
- Say: "Every test is repeatable and leaves the database exactly as it was."

**Slide 15 — Conclusion & live demo**
- Delivered: 100 % of the proposed scope; future work: trigger for the cross-section rule, sequences, CGPA, audit log
- Demo steps (§13.2) · Thank you / Q&A

**Backup slides:** B1 functional dependency table (§5.1); B2 constraint enforcement matrix (§6.2); B3 HTTP status code semantics (400/401/403/404/409).

---

# PART C — ARTEFACT INDEX

| Artefact | Path |
|---|---|
| Schema DDL | `sql/01_create_schema.sql` |
| Seed DML | `sql/02_insert_sample_data.sql`, `sql/03_expand_sample_data.sql` |
| Logins | `sql/04_authentication_migration.sql`, `sql/04_provision_dev_students.sql` |
| Phase 3 migration | `database/phase3/01_audit_row_counts.sql`, `01_audit_all.sql`, `02_migrate_schema_and_data.sql`, `03_verify_post_migration.sql` |
| Backend | `SmartCampus/src/main/java/com/campus/smartcampus/` (config, controller, service, repository, dto, model, exception) |
| Front end | `SmartCampus/src/main/resources/static/` |
| Tests | `SmartCampus/src/test/java/…/Phase3IntegrationTest.java`, `model/MarksPolicyTest.java` |
| Workflow suite | `scripts/verify_workflows.ps1` |
| Screenshot capture | `scripts/capture_screenshots.js` |
| Launchers | `run.bat`, `run.ps1` |
| Phase 3 docs | `docs/phase3/` (implementation report, CRUD matrix, test plan, migration guide, demo & viva guide, presentation outline, known issues) |
| Evidence | `docs/phase3/test-evidence/`, `docs/phase3/screenshots/` |
