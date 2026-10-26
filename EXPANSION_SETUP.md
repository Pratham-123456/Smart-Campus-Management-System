# Smart Campus Management System — Database Expansion & Setup Guide
**XYZ University Information System** &bull; College Database Systems Project

---

## 1. Overview of Database Expansion

The database has been expanded with realistic, relationally consistent synthetic records across all 14 academic tables while preserving all pre-existing records:

| Academic Table | Total Records | Description |
|---|---:|---|
| `DEPARTMENT` | **6** | Computer Science, Electronics, Mechanical, Civil, IT, Mathematics & Data Sciences |
| `PROGRAM` | **10** | B.Tech & M.Tech degree curriculums spanning 2 to 4 years |
| `STUDENT` | **60** | Students (#1001 to #1060) distributed across all academic programs |
| `FACULTY` | **15** | Appointed professors, associate professors, and assistant professors (#201 to #215) |
| `COURSE` | **30** | Core and elective subjects across all university departments (#301 to #330) |
| `COURSE_OFFERING` | **45** | Semester offerings (#401 to #445) with assigned rooms, sections, and faculty |
| `ENROLLMENT` | **365** | Course registrations (no section exceeds capacity) |
| `ATTENDANCE` | **365** | Subject attendance tracking with realistic distribution (`Attended <= Held`) |
| `MARKS` | **250** | Continuous Assessment (30), Midterm (30), and Final (50) marks breakdowns |
| `GRADE` | **6** | Compatible academic grading scale: S (10), A (9), B (8), C (7), D (6), F (0) |
| `EXAM` | **42** | CAT1, CAT2, and FAT examinations (#801 to #845) with venue and time slots |
| `EXAM_REGISTRATION` | **232** | Verified student registrations for enrolled offerings (#901 to #1136) |
| `EXAM_RESULT` | **190** | Published evaluated results with scores, percentages, and letter grades |
| `RE_EXAM_APPLICATION` | **20** | Re-examination petitions with PENDING, APPROVED, and REJECTED statuses |
| `USER_ACCOUNT` | **76** | Multi-role accounts (60 Students + 15 Faculty + 1 Admin) |

---

## 2. Scripts and Automation Utilities

* **`sql/03_expand_sample_data.sql`**: Idempotent SQL script that populates the 14 academic tables using `WHERE NOT EXISTS` guards.
* **`sql/04_authentication_migration.sql`**: Additive authentication script that ensures the `USER_ACCOUNT` table exists and provisions all accounts with BCrypt-hashed password `1234`.
* **`scripts/provision_dev_accounts.ps1`**: Controlled PowerShell utility to provision or reset accounts across all roles.

---

## 3. Temporary Development Credentials

> [!WARNING]
> **Academic Development Notice:**
> The password `1234` is a temporary, weak credential designated strictly for local demonstration and evaluation. Passwords are salted and hashed with BCrypt (`$2a$10$...`) inside the Oracle database. In a production deployment, individual passwords and mandatory resets on first login must be enforced.

### Role Login Guide:
* **Student Portal**:
  * Role: **Student**
  * Login Identifier: Any Student ID from **`1001`** to **`1060`** (e.g. `1001`, `1002`, `1010`, `1025`)
  * Password: **`1234`**
* **Faculty Portal**:
  * Role: **Faculty**
  * Login Identifier: Any Faculty ID from **`201`** to **`215`** (e.g. `201`, `202`, `205`, `210`)
  * Password: **`1234`**
* **Administrator Portal**:
  * Role: **Administrator**
  * Login Identifier: **`admin`**
  * Password: **`1234`**

---

## 4. Running the Application

### 1. Database (Docker):
```powershell
docker start oracle-db
```
* **Host**: `localhost` &bull; **Port**: `1521` &bull; **Service**: `FREEPDB1` &bull; **User**: `campus_app`

### 2. Spring Boot Web Application:
```powershell
cd SmartCampus
$env:CAMPUS_DB_PASSWORD = [Environment]::GetEnvironmentVariable("CAMPUS_DB_PASSWORD", "User")
java "-Dspring.datasource.password=$env:CAMPUS_DB_PASSWORD" -jar target\smart-campus-1.0.0.jar
```

### 3. Access Web Portal:
Open your browser at:
**[http://localhost:8080](http://localhost:8080)**

---

## 5. Key Verified Features

1. **Live Table Search Filtering**: Instant real-time search inputs on large directories (Students, Faculty, Offerings, Exams, Registrations).
2. **Dynamic Identity Verification**: No hardcoded records or bypasses; every student, faculty, and administrator dashboard is derived dynamically from their authenticated session.
3. **Examination Results & Re-Exams**: Full published result breakdown (scores, percentages, S/A/B/C/D/F grades) with active re-exam petition workflows.
4. **Attendance Analytics**: Subject-level attendance monitoring with statutory 75% requirement badges.
