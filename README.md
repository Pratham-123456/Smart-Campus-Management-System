# Student Academic & Examination Management System
### Smart Campus Management System · XYZ University Academic Services Portal

> **BACSE202 — Database Systems · Course Project (CO5) · Fall Semester 2026 · VIT SCOPE**
> A VTOP-inspired academic and examination portal: a normalised Oracle database interfaced with a web front end through **JDBC**, with full CRUD.

| Team member | Register No. |
|---|---|
| Pratham Garg | 25BCE2016 |
| S Rohith | 25BCE0790 |
| Divyam Kurkan | 25BCE2041 |

**Stack:** Java 21 · Spring Boot 3.3.4 · Spring JDBC (`JdbcTemplate`) · HikariCP · Oracle AI Database 26ai Free (Docker) · HTML5 / CSS3 / vanilla JavaScript SPA · JUnit 5 + MockMvc

📄 **Final report (all 11 stages + presentation content):** [`docs/FINAL_PROJECT_REPORT.md`](docs/FINAL_PROJECT_REPORT.md)

---

## At a glance

| | |
|---|---|
| Functional requirements | 12 (5 student-facing, 7 admin/faculty) |
| ER model | 14 entities, 18 relationships (Chen notation) |
| Normalisation | UNF → 1NF (4) → 2NF (6) → 3NF (11) → 14 tables |
| Oracle schema | **15 tables, 20 foreign keys, 11 named CHECK constraints** |
| Seed data | **1,712 rows** (1,636 academic + 76 BCrypt logins); 1,736 live |
| REST API | 69 endpoints; CRUD on every table |
| Verification | Oracle probes 23/23 · SQL audit 0 violations · JUnit 36/36 · workflow suite 46/46 |

## Project phases

| Phase | Assessment | Deliverable | Status |
|---|---|---|---|
| 1 — Design Foundation | 8 (30 %) | Requirements, ER/EER, relational mapping, 1NF–3NF | ✅ Review 1 |
| 2 — Implementation Core | 9 (80 %) | DDL, DML, front end, business logic, initial JDBC | ✅ Review 2 |
| 3 — Integration & Delivery | 10 (100 %) | Complete JDBC CRUD, testing, report, final PPT, demo | ✅ This repository |

---

## Features

### 👨‍🎓 Student
- Profile, plus course registration with live seats (`enrolled / Max_Seats`) and search
- **Drop** an active course, from the registration page or the marks page
- Attendance per course with computed % and a shortage warning below 75 %
- Marks shown as CA /30, Midterm /30 and Final /50, with a **course total out of 100**, letter grade, grade point and GPA, plus a printable grade sheet
- Exam timetable (CAT1, CAT2, FAT): **register** or **cancel**, with clash detection
- Published results with grade, and **re-exam application**: apply, track PENDING / APPROVED / REJECTED, or withdraw

### 👩‍🏫 Faculty (own offerings only)
- Assigned offerings, class roster with attendance % and marks
- Edit Classes Held, record attendance (0 ≤ attended ≤ held) and marks (30 / 30 / 50)
- Invigilation schedule

### 🛡️ Administrator
- Live KPI dashboard (students, faculty, courses, offerings, enrollments, pending re-exams, departments/programs, exams/results)
- **Create / edit / delete** departments, programs, courses, offerings, students and faculty. A record that other records still depend on returns `409` and lists what references it.
- Exam scheduling, **Mark Completed** (only after the exam has ended), Cancel, Edit, Delete
- **Result publication:** the grade and PASS/FAIL are derived from the GRADE table; results can be withheld, edited or deleted
- Re-exam decisions (`PENDING → APPROVED / REJECTED`)
- Account provisioning (BCrypt), plus a user accounts page to activate, deactivate, reset passwords and delete

### Business rules (enforced in the service layer, backed by Oracle constraints)
| Rule | Response |
|---|---|
| Same offering twice / another section of the same course in the same term / offering full | `409 Conflict` |
| Overlapping exam registration on the same date (back-to-back allowed) | `409 Conflict` |
| Completing an exam or publishing results before the exam has ended | `409 Conflict` |
| Attendance above Classes_Held, marks above 30 / 30 / 50, semester outside 1–10 | `400 Bad Request` |
| Another user's record or the wrong role | `403 Forbidden` · no session → `401` |
| Unknown id | `404 Not Found` |

**Marks policy:** the component sum is out of 110 (30 + 30 + 50), so the course **total is scaled to 100** (sum × 100 ÷ 110) and the grade is taken from it.

---

## Architecture

```
Browser (HTML5 · CSS3 · vanilla JS SPA)
   │  fetch() + session cookie (JSON)
   ▼
AuthInterceptor ── 401 no session · 403 wrong role / another user's id
   ▼
REST controllers (Auth, Student, Faculty, Admin) + GlobalExceptionHandler (400/403/404/409, no SQL leaked)
   ▼
Services — business rules, @Transactional
   ▼
JDBC repositories — JdbcTemplate → PreparedStatement ('?' binds only)
   ▼
HikariCP pool → Oracle AI Database 26ai Free · FREEPDB1 · Docker container oracle-db :1521
```
The database credentials stay on the server, read from the `CAMPUS_DB_PASSWORD` environment variable. Nothing is hardcoded in the repository.

---

## Database schema (Oracle 26ai)

| # | Table | Primary key | Foreign keys | Notable constraints |
|---:|---|---|---|---|
| 1 | DEPARTMENT | Department_ID | — | UNIQUE Department_Name |
| 2 | PROGRAM | Program_ID | Department_ID | UNIQUE Program_Name, Duration > 0 |
| 3 | STUDENT | Student_ID | Program_ID | UNIQUE Email |
| 4 | FACULTY | Faculty_ID | Department_ID | UNIQUE Email |
| 5 | COURSE | Course_ID | Department_ID | Credits > 0 |
| 6 | COURSE_OFFERING | Offering_ID | Course_ID, Faculty_ID | UNIQUE (Course, Semester, Year, Section); **Semester NUMBER(2) 1–10**; Max_Seats > 0 |
| 7 | ENROLLMENT | Enrollment_ID | Student_ID, Offering_ID | UNIQUE (Student, Offering); CK_ENROLLMENT_STATUS |
| 8 | ATTENDANCE | Attendance_ID | Student_ID, Offering_ID | UNIQUE (Student, Offering); ≥ 0 |
| 9 | MARKS | Mark_ID | Student_ID, Offering_ID | UNIQUE (Student, Offering); CK_MARKS_CA / _MIDTERM / _FINAL (30 / 30 / 50) |
| 10 | GRADE | Grade_Code | — | CK_GRADE_RANGE |
| 11 | EXAM | Exam_ID | Offering_ID | CK_EXAM_TIME (End > Start); CK_EXAM_STATUS |
| 12 | EXAM_REGISTRATION | Exam_Registration_ID | Student_ID, Exam_ID | UNIQUE (Student, Exam); CK_EXAMREG_STATUS |
| 13 | EXAM_RESULT | Exam_Result_ID | Exam_Registration_ID (UNIQUE, 1:1), Grade_Code | CK_RESULT_STATUS |
| 14 | RE_EXAM_APPLICATION | ReExam_Application_ID | Exam_Result_ID | CK_REEXAM_STATUS |
| 15 | USER_ACCOUNT | Account_ID | Student_ID, Faculty_ID | UNIQUE Username; CK_ACCOUNT_REF; BCrypt hash |

3NF design notes: `Classes_Held` lives in COURSE_OFFERING (it depends on the offering alone), and RE_EXAM_APPLICATION does not store `Student_ID` (it is reached through the result). Attendance % and totals are computed, never stored.

---

## Quick start

**Prerequisites:** Java 21, Docker Desktop with the Oracle 26ai Free container `oracle-db` (port 1521, service `FREEPDB1`, user `campus_app`), and Maven 3.9+ (or the included `mvnw.cmd`).

1. **Save the database password once.** The launch scripts read it from this variable and prompt if it is missing.
   ```powershell
   setx CAMPUS_DB_PASSWORD "<your-db-password>"
   ```
2. **Create the database:**
   - **Fresh database:** run `sql/01_create_schema.sql` → `02_insert_sample_data.sql` → `03_expand_sample_data.sql` → `04_authentication_migration.sql`.
   - **Existing database from Phase 2:** apply `database/phase3/` (see [Migration guide](docs/phase3/MIGRATION_GUIDE.md)).
3. **Run the app.** `run.bat` checks Docker, starts `oracle-db` if it is stopped, launches the server on port 8080 and opens the browser:
   ```cmd
   run.bat
   ```
   Alternatives: `powershell -ExecutionPolicy Bypass -File .\run.ps1`, or `cd SmartCampus` then `.\mvnw.cmd spring-boot:run`.
4. Open **http://localhost:8080**.

### Demo accounts (development password `1234`, stored only as BCrypt hashes)

| Role | Username | Name |
|---|---|---|
| Student | `1001` | Aarav Sharma — B.Tech Computer Science Engineering |
| Student | `1010` | Arjun Gupta — B.Tech Mechanical Engineering |
| Student | `1041` | Shreya Yadav — B.Tech Robotics and Industrial Automation (no published results yet) |
| Faculty | `201` | Dr. Meera Rao — Computer Science |
| Faculty | `205` | Prof. Rajesh Nair — Computer Science |
| Administrator | `admin` | System Administrator |

---

## Testing

| Suite | Command | Result |
|---|---|---|
| Oracle constraint probes (rolled back) | `@database/phase3/03_verify_post_migration.sql` | 23 / 23 |
| Data audit (all Review 2 findings) | `@database/phase3/01_audit_all.sql` | 0 violations |
| JUnit + MockMvc against live Oracle (rolled back) | `cd SmartCampus` then `mvn test` | 36 / 36 |
| End-to-end HTTP workflows (self-cleaning) | `powershell -ExecutionPolicy Bypass -File scripts\verify_workflows.ps1` | 46 / 46 |
| Screenshots | `node scripts/capture_screenshots.js` | 23 PNGs |

Evidence: [`docs/phase3/test-evidence/`](docs/phase3/test-evidence/) · test plan: [`docs/phase3/TEST_PLAN_AND_RESULTS.md`](docs/phase3/TEST_PLAN_AND_RESULTS.md)

---

## Documentation

| Document | Purpose |
|---|---|
| [`docs/FINAL_PROJECT_REPORT.md`](docs/FINAL_PROJECT_REPORT.md) | **Complete final report** (11 stages), slide-by-slide presentation content, artefact index |
| [`docs/phase3/PHASE3_IMPLEMENTATION_REPORT.md`](docs/phase3/PHASE3_IMPLEMENTATION_REPORT.md) | Phase 3 implementation details |
| [`docs/phase3/CRUD_COVERAGE.md`](docs/phase3/CRUD_COVERAGE.md) | CRUD matrix for all 15 tables (endpoints, layers, constraints, tests) |
| [`docs/phase3/TEST_PLAN_AND_RESULTS.md`](docs/phase3/TEST_PLAN_AND_RESULTS.md) | Test cases and results |
| [`docs/phase3/MIGRATION_GUIDE.md`](docs/phase3/MIGRATION_GUIDE.md) | Applying the Phase 3 migration to an existing database |
| [`docs/phase3/DEMO_AND_VIVA_GUIDE.md`](docs/phase3/DEMO_AND_VIVA_GUIDE.md) | 5-minute demo script and viva question bank |
| [`docs/phase3/FINAL_PRESENTATION_OUTLINE.md`](docs/phase3/FINAL_PRESENTATION_OUTLINE.md) | 15-slide outline |
| [`docs/phase3/KNOWN_ISSUES.md`](docs/phase3/KNOWN_ISSUES.md) | Data reconciliation decisions and limitations |
| `PROJECT_REPORT.md`, `SMART_CAMPUS_SYSTEM_DOCUMENTATION.pdf` | Earlier (Phase 2) reports |

---

## Repository layout

```
├── run.bat / run.ps1                  # Launchers (Docker check → Oracle → Spring Boot :8080)
├── sql/                               # Fresh-install scripts
│   ├── 01_create_schema.sql           #   DDL: 14 academic tables + constraints
│   ├── 02_insert_sample_data.sql      #   Seed data (45 rows)
│   ├── 03_expand_sample_data.sql      #   Expanded data (1,591 rows, idempotent)
│   └── 04_authentication_migration.sql#   USER_ACCOUNT + 76 BCrypt logins
├── database/phase3/                   # Live-database audit → migrate → verify (idempotent)
├── scripts/
│   ├── verify_workflows.ps1           #   46-scenario end-to-end suite
│   └── capture_screenshots.js         #   Headless-Chrome screenshot capture
├── docs/
│   ├── FINAL_PROJECT_REPORT.md        #   Final report + presentation content
│   └── phase3/                        #   Phase 3 docs, screenshots/, test-evidence/
└── SmartCampus/                       # Spring Boot application
    ├── pom.xml
    └── src/
        ├── main/java/com/campus/smartcampus/
        │   ├── config/                #   AuthInterceptor, WebMvc, DataSource
        │   ├── controller/            #   Auth, Student, Faculty, Admin + GlobalExceptionHandler
        │   ├── service/               #   Business rules (@Transactional)
        │   ├── repository/            #   JDBC repositories (SQL)
        │   ├── dto/  model/  exception/
        ├── main/resources/static/     #   index.html, css/, js/ (api, app, student, faculty, admin, admin-crud)
        └── test/java/…                #   Phase3IntegrationTest, MarksPolicyTest
```

---

Developed for academic purposes as part of **BACSE202 — Database Systems**, VIT. "XYZ University" is the project's fictional institution.
