# Smart Campus Management System — Academic Web Portal

A full-stack university academic management system built with **Java 21**, **Spring Boot 3.3.4**, and **Oracle AI Database 26ai Free**, featuring a modern **VTOP-inspired single-page responsive web portal**.

---

## 1. Quick Start & Execution Guide

### Prerequisites
- **Java 21 LTS** (`java -version`)
- **Oracle Database 26ai Free** running in Docker on port `1521` (Service: `FREEPDB1`)
- **Maven 3.9+** (configured in user directory or via `./mvnw.cmd`)

### Database Connection Password
The database password for `campus_app` is read securely from the environment and is never hardcoded.

In Windows PowerShell:
```powershell
$env:CAMPUS_DB_PASSWORD = "<your-db-password>"
```

### Running the Application

You can run the application directly using either the Maven wrapper or the pre-compiled JAR:

**Option A — Run with Maven:**
```powershell
cd SmartCampus
.\mvnw.cmd spring-boot:run
```

**Option B — Run the Standalone JAR:**
```powershell
cd SmartCampus
java -jar target\smart-campus-1.0.0.jar
```

### Accessing the Website
Open your browser and navigate to:
```
http://localhost:8080/
```

---

## 2. Features Implemented vs Incomplete Matrix

| Module | Feature | Status | Implementation Details |
| :--- | :--- | :---: | :--- |
| **Student** | Login & Academic Dashboard | ✅ Complete | Verified against `STUDENT` table; summary metrics for courses, attendance, exams. |
| **Student** | Profile & Program Details | ✅ Complete | Degree, department, admission year, contact details from `PROGRAM` & `DEPARTMENT`. |
| **Student** | Registered Courses & Offerings | ✅ Complete | Lists enrollments and available offerings with live seat capacity counts. |
| **Student** | Course Registration | ✅ Complete | Prevents duplicate enrollments (`UQ_ENROLLMENT`), enforces max seat capacity. |
| **Student** | Attendance Records & % | ✅ Complete | Calculates attendance percentage; warns on shortage (< 75%). |
| **Student** | Marks & Grade Information | ✅ Complete | Continuous Assessment (CA), Midterm, Final exam breakdown and grade. |
| **Student** | Exam Timetable & Registration | ✅ Complete | Timetables for enrolled courses; one-click exam registration (`UQ_EXAMREG`). |
| **Student** | Published Exam Results | ✅ Complete | Official marks, percentage, grade, pass/fail status from `EXAM_RESULT`. |
| **Student** | Re-Exam Application & Status | ✅ Complete | Modal submission with reason validation; real-time status tracking. |
| **Faculty** | Assigned Offerings View | ✅ Complete | Course offerings assigned to the logged-in faculty member. |
| **Faculty** | Enrolled Students Roster | ✅ Complete | List of students enrolled in each assigned section with performance data. |
| **Faculty** | Classes Held Counter | ✅ Complete | Edit total classes conducted for an offering. |
| **Faculty** | Enter / Update Attendance | ✅ Complete | Validates `Classes_Attended <= Classes_Held` and non-negative numbers. |
| **Faculty** | Enter / Update Marks | ✅ Complete | Validates CA ($\le 30$), Midterm ($\le 30$), Final ($\le 50$). |
| **Faculty** | Scheduled Exam Timetables | ✅ Complete | View upcoming exams scheduled for assigned sections. |
| **Admin** | Departments Management | ✅ Complete | View all departments, add new department. |
| **Admin** | Degree Programs Management | ✅ Complete | View programs, add new program (linked to department FK). |
| **Admin** | Student Directory Management | ✅ Complete | View students, enroll new student (linked to program FK). |
| **Admin** | Faculty Directory Management | ✅ Complete | View faculty, add faculty (linked to department FK). |
| **Admin** | Master Course Catalog | ✅ Complete | View curriculum courses, add new courses. |
| **Admin** | Course Offerings Creation | ✅ Complete | Schedule semester offerings with room, faculty, section, max seats. |
| **Admin** | Examination Scheduling | ✅ Complete | Schedule CAT1, CAT2, FAT exams with date, time, venue, max marks. |
| **Admin** | Exam Results Registry | ✅ Complete | Master view of all evaluated and published examination scores. |
| **Admin** | Re-Exam Application Decisions | ✅ Complete | Review student petitions with instant Approve / Reject actions. |

*All 24 core requirements specified in the project brief are 100% implemented, verified, and backed by the Oracle schema.*

---

## 3. Technology Stack & Architecture

- **Backend**: Java 21, Spring Boot 3.3.4, Spring JDBC (`JdbcTemplate`), HikariCP Connection Pool.
- **Frontend**: HTML5, CSS3, Vanilla JavaScript ES6 (SPA served from Spring Boot static resources).
- **Database**: Oracle AI Database 26ai Free (Docker container `oracle-db`, service `FREEPDB1`, user `campus_app`).
- **Design Inspiration**: VTOP-style academic portal with modern Navy-Blue (`#0a192f`, `#1a365d`) palette, responsive flex/grid layouts, card widgets, data tables, and modal dialogues.

---

## 4. Security & Demonstration Authentication Note

- In this academic development environment, authentication uses the student or faculty member's institutional ID and email address verified directly against the Oracle `STUDENT` and `FACULTY` tables.
- In a production environment, this would be replaced with salted password hashing (e.g. bcrypt/PBKDF2), Multi-Factor Authentication (MFA), and signed JWT tokens with short TTLs.
- Database credentials remain strictly on the backend server via `CAMPUS_DB_PASSWORD` and are never sent to the browser client.
