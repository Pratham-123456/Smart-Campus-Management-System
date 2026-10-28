# XYZ University — Smart Campus Management System
> **Academic Services Portal & Relational Database System**  
> *Developed for BACSE202 — Database Systems Course Project*

A full-stack, enterprise-grade university academic information portal built with **Java 21**, **Spring Boot 3.3.4**, **Oracle AI Database 26ai Free**, and a modern **Vanilla JavaScript SPA (Single-Page Application)** frontend.

---

## 🌟 Key Features

### 👨‍🎓 Student Portal
- **Academic Dashboard**: Real-time summary of enrollments, attendance percentages, continuous assessment marks, and exam schedules.
- **Course Registration**: Browse available semester offerings with live seat capacity tracking (`enrolled / maxSeats`), preventing duplicate enrollments and over-capacity registrations.
- **Attendance Tracking**: Visual attendance rate indicators with warning alerts for attendance shortage (< 75%).
- **Continuous Assessment & Marks**: Breakdown of Continuous Assessment (CA $\le$ 30), Midterm ($\le$ 30), Final Exam ($\le$ 50), Total marks, and letter grades.
- **Examinations**: Timetables for CAT1, CAT2, and FAT with one-click exam registration.
- **Official Results & Grade Cards**: Dynamic percentage calculation mapped strictly to the authoritative university grade scale (S, A, B, C, D, F).
- **Re-Examination Petitions**: Modal application submission for grade re-evaluation with real-time status tracking (`PENDING`, `APPROVED`, `REJECTED`).

### 👩‍🏫 Faculty Portal
- **Course Sections Overview**: List of assigned course offerings and classroom schedules.
- **Student Roster Management**: Live roster of all enrolled students with attendance records and performance metrics.
- **Attendance Grading**: Update total classes conducted and record individual student attendance with boundary validation (`Classes_Attended <= Classes_Held`).
- **Marks Entry Gradebook**: Enter and persist Continuous Assessment, Midterm, and Final examination marks.
- **Exam Invigilation Schedule**: View assigned supervisory duties and examination sessions.

### 🛡️ Administrator Portal
- **Real-Time KPI Dashboard**: University-wide counters for Departments, Programs, Faculty, Students, Offerings, and Exams.
- **Institutional Management**: CRUD management for Departments, Degree Programs, Faculty, Students, and Courses.
- **Course Offering Scheduler**: Schedule semester sections with faculty assignments, room allocations, and seat capacities.
- **Examination Scheduler**: Schedule CAT1, CAT2, and FAT examinations with dates, venues, and timings.
- **Re-Exam Petition Decision Center**: Review submitted re-evaluation requests with instant **Approve** or **Reject** actions.
- **Multi-Role User Provisioning**: On-demand BCrypt user account creation and credential management.

---

## 🏛️ System Architecture

```
[ Client Web Browser ]  (Single Page Application: HTML5, CSS3, JavaScript ES6)
         │
         ▼  HTTP REST (Session Cookie Authentication)
[ Security & Interceptor Layer ] (AuthInterceptor: 401 Unauthorized / 403 Forbidden Role Isolation)
         │
         ▼
[ Spring Boot 3.3.4 Backend ]  (REST Controllers & Business Validation Services)
         │
         ▼  JDBC (HikariCP High-Performance Connection Pool)
[ Oracle Database 26ai Free ]  (14 Academic Tables + USER_ACCOUNT Security Table)
```

---

## 📊 Relational Database Schema (Oracle 26ai)

The system is backed by **14 core academic tables** and **1 authentication table**:

| # | Table Name | Description | Key Attributes | Live Records |
| :---: | :--- | :--- | :--- | :---: |
| 1 | `DEPARTMENT` | Academic Departments | `Dept_ID (PK)`, `Name`, `Code` | 6 |
| 2 | `PROGRAM` | Degree Programs | `Program_ID (PK)`, `Dept_ID (FK)`, `Degree_Level` | 10 |
| 3 | `FACULTY` | Professors & Instructors | `Faculty_ID (PK)`, `Dept_ID (FK)`, `Designation` | 15 |
| 4 | `STUDENT` | University Students | `Student_ID (PK)`, `Program_ID (FK)`, `Admission_Year` | 60 |
| 5 | `COURSE` | Master Course Catalog | `Course_ID (PK)`, `Dept_ID (FK)`, `Credits` | 30 |
| 6 | `COURSE_OFFERING` | Semester Course Sections | `Offering_ID (PK)`, `Course_ID (FK)`, `Faculty_ID (FK)`, `Max_Seats` | 45 |
| 7 | `ENROLLMENT` | Course Registrations | `Enrollment_ID (PK)`, `Student_ID (FK)`, `Offering_ID (FK)` | 365 |
| 8 | `ATTENDANCE` | Course Attendance Logs | `Attendance_ID (PK)`, `Student_ID (FK)`, `Classes_Attended` | 365 |
| 9 | `MARKS` | Continuous Assessment | `Mark_ID (PK)`, `Continuous_Assessment_Marks`, `Final_Exam_Marks` | 250 |
| 10 | `GRADE` | University Letter Grades | `Grade_Code (PK)`, `Min_Percentage`, `Max_Percentage`, `Grade_Point` | 6 |
| 11 | `EXAM` | Examination Schedules | `Exam_ID (PK)`, `Offering_ID (FK)`, `Exam_Type`, `Exam_Date` | 42 |
| 12 | `EXAM_REGISTRATION` | Student Exam Seats | `Exam_Registration_ID (PK)`, `Student_ID (FK)`, `Exam_ID (FK)` | 232 |
| 13 | `EXAM_RESULT` | Published Results | `Exam_Result_ID (PK)`, `Marks_Obtained`, `Grade_Code (FK)` | 190 |
| 14 | `RE_EXAM_APPLICATION` | Re-Evaluation Petitions | `ReExam_Application_ID (PK)`, `Exam_Result_ID (FK)`, `Status` | 20 |
| 15 | `USER_ACCOUNT` | Multi-Role Authentication | `User_ID (PK)`, `Username (UQ)`, `Password_Hash`, `Role` | 76 |

---

## 🚀 Quick Start Guide

### Prerequisites
- **Java 21 LTS** (`java -version`)
- **Docker Desktop** (with Oracle 26ai Free container running on port `1521`)
- **Maven 3.9+** (or use the included `./mvnw.cmd` wrapper)

### Running the Application

**Option 1: Windows One-Click Launcher (Recommended)**
```cmd
run.bat
```
*Automatically inspects Docker, starts `oracle-db` if stopped, configures credentials, launches Spring Boot on port 8080, and opens your browser.*

**Option 2: PowerShell Launcher**
```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

**Option 3: Manual Launch via Maven**
```powershell
cd SmartCampus
$env:CAMPUS_DB_PASSWORD = "<your-db-password>"
.\mvnw.cmd spring-boot:run
```

Once started, open **[http://localhost:8080](http://localhost:8080)** in your browser.

---

## 🔑 Demonstration Credentials

All accounts in the development environment are configured with the universal password **`1234`** (hashed using BCrypt):

| Portal Role | Username / ID | Full Name | Program / Department |
| :--- | :--- | :--- | :--- |
| **Student** | `1001` | Aarav Sharma | B.Tech Computer Science and Engineering |
| **Student** | `1010` | Ananya Roy | B.Tech Computer Science and Engineering |
| **Student** | `1041` | Tanisha Das | Master of Computer Applications (MCA) |
| **Faculty** | `201` | Dr. Rajesh Sharma | Department of Computer Science |
| **Faculty** | `205` | Dr. Sneha Reddy | Department of Electronics and Communication |
| **Administrator** | `admin` | System Administrator | University Administration |

---

## 🧪 Automated End-to-End Verification Suite

The repository includes a complete automated verification test runner (`scripts/verify_workflows.ps1`) covering the 15 original end-to-end workflow scenarios (Part A) plus 31 Phase 3 scenarios (Part B, 46 in total):

```powershell
powershell -ExecutionPolicy Bypass -File scripts\verify_workflows.ps1
```

All 15 scenarios passed with 100% compliance:
- [x] Student authentication and active enrollment viewing
- [x] Duplicate course registration rejection (`HTTP 409 Conflict`)
- [x] Full seat capacity enforcement (`HTTP 409 Conflict`)
- [x] Course registration persistence in Oracle DB
- [x] Faculty attendance recording & student verification
- [x] Faculty marks entry & gradebook calculation
- [x] Published result viewing with authoritative letter grades
- [x] Clean zero-result empty state rendering
- [x] Re-examination application lifecycle (`PENDING` tracking)
- [x] Cross-faculty unauthorized offering editing rejection (`HTTP 403 Forbidden`)
- [x] Administrative global entity and statistics retrieval
- [x] Responsive table formatting without horizontal overflow
- [x] Real-time client-side table search filtering (`UI.filterTable`)
- [x] Meaningful error messaging for invalid inputs
- [x] Clean application logs without unhandled exceptions

---

## 🧩 Phase 3: Integration & Delivery

All eight Review 2 Appendix A findings are fixed in the code, the seed scripts and the live database, and every table now has create, read, update and delete (or logical retirement) end to end.

| Area | Where |
|---|---|
| Live-database migration (audit → migrate → verify, idempotent) | `database/phase3/` · guide: `docs/phase3/MIGRATION_GUIDE.md` |
| Implementation report (26 sections) | `docs/phase3/PHASE3_IMPLEMENTATION_REPORT.md` |
| CRUD matrix for 15 tables | `docs/phase3/CRUD_COVERAGE.md` |
| Test plan and results | `docs/phase3/TEST_PLAN_AND_RESULTS.md`, evidence in `docs/phase3/test-evidence/` |
| Screenshots (23) | `docs/phase3/screenshots/` (regenerate: `node scripts/capture_screenshots.js`) |
| Demo script, viva bank, slide outline, known issues | `docs/phase3/` |

Verification on 28-Oct-2026: Oracle constraint probes 23/23 · SQL audit 0 violations · `mvn test` 36/36 (live Oracle, rolled back) · `verify_workflows.ps1` 46/46 with live data unchanged.

Marks policy: CA /30 + Midterm /30 + Final /50; the course **total is out of 100** (sum × 100 ÷ 110, never above 100) and the grade is taken from it. Students can drop an active course from the registration page or the marks page.

---

## 📁 Project Directory Structure

```
Database Systems/
├── run.bat                               # Windows one-click application starter
├── run.ps1                               # PowerShell application launcher
├── README.md                             # Project documentation & overview
├── EXPANSION_SETUP.md                    # Database expansion & setup guide
├── PROJECT_REPORT.md                     # Course project report
├── SMART_CAMPUS_SYSTEM_DOCUMENTATION.pdf # Full 10-page technical specification PDF
├── dataset_export.txt                    # Text dump of the seeded data (1,896 LINES incl. headers; 1,712 seeded ROWS)
├── docs/                                 # Documentation assets
│   └── project_documentation.html        # Printable technical specification HTML
├── sql/                                  # Database SQL scripts
│   ├── 01_create_schema.sql              # Relational DDL for 14 academic tables
│   ├── 02_insert_sample_data.sql         # Seed sample dataset
│   ├── 03_expand_sample_data.sql         # Expanded dataset (365 enrollments, 250 marks)
│   └── 04_authentication_migration.sql   # USER_ACCOUNT table and BCrypt provisioning
├── scripts/                              # Utility and verification scripts
│   ├── verify_workflows.ps1              # 15-scenario automated verification test runner
│   ├── provision_dev_accounts.ps1        # Database account provisioner
│   └── provision_dev_students.ps1        # Student account provisioning utility
└── SmartCampus/                          # Spring Boot & Frontend Application
    ├── pom.xml                           # Maven project descriptor
    ├── mvnw.cmd                          # Maven wrapper
    └── src/main/
        ├── java/com/campus/smartcampus/  # Backend Java 21 source code
        │   ├── SmartCampusApplication.java
        │   ├── config/                   # HikariCP, WebMvc, AuthInterceptor
        │   ├── controller/               # Auth, Student, Faculty, Admin REST controllers
        │   ├── dto/                      # Data Transfer Objects with validation
        │   ├── model/                    # Domain models for all 15 tables
        │   ├── repository/               # JDBC repositories with SQL queries
        │   └── service/                  # Business logic & validation services
        └── resources/
            ├── application.properties    # Server & datasource configuration
            └── static/                   # Frontend SPA web assets
                ├── index.html            # Main SPA container & layout
                ├── css/portal.css        # University styling & responsive system
                └── js/                   # Client controllers (app, api, student, faculty, admin)
```

---

## 📄 License

Developed for academic purposes at **XYZ University** as part of the **BACSE202 — Database Systems** curriculum.
