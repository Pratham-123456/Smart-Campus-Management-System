# SmartCampus — Spring Boot application

The web application for the **Student Academic & Examination Management System**, the BACSE202 Database Systems course project. See the [root README](../README.md) for the project overview and the [final report](../docs/FINAL_PROJECT_REPORT.md) for full details.

**Stack:** Java 21 · Spring Boot 3.3.4 (web, jdbc, validation) · `JdbcTemplate` + HikariCP · ojdbc11 → Oracle AI Database 26ai Free (`FREEPDB1`, user `campus_app`) · Spring Security Crypto (BCrypt) · static HTML/CSS/JS SPA · JUnit 5 + MockMvc

---

## Run

The database password is read from the environment and is never hardcoded:
```powershell
setx CAMPUS_DB_PASSWORD "<your-db-password>"     # once; open a new terminal afterwards
```

From this folder, either run with Maven:
```powershell
.\mvnw.cmd spring-boot:run
```
or build the jar and run it:
```powershell
.\mvnw.cmd -DskipTests package
java -jar target\smart-campus-1.0.0.jar
```
Open http://localhost:8080. The repository-root `run.bat` does the same and also starts the Docker container.

## Test

```powershell
mvn test
```
This runs **36 tests**:
* `MarksPolicyTest` is a unit test of the /100 course-total rule.
* `Phase3IntegrationTest` covers course and exam registration rules, faculty updates, results and re-exams, Oracle CHECK constraints, admin CRUD with FK protection, and 401/403 access control.

The integration tests run against the real Oracle schema inside a transaction that is **rolled back**, so no data changes. They are skipped when `CAMPUS_DB_PASSWORD` is not set.

---

## Code layout (`src/main/java/com/campus/smartcampus`)

| Package | Contents |
|---|---|
| `config` | `AuthInterceptor` (session check, role isolation, same-user check on `/api/student/{id}` and `/api/faculty/{id}`), `WebMvcConfig`, `DatabaseConfig` |
| `controller` | `AuthController` (`/api/auth`), `StudentController` (`/api/student/{id}`), `FacultyController` (`/api/faculty/{id}`), `AdminController` (`/api/admin`), `GlobalExceptionHandler` (400/403/404/409/500 JSON; never exposes SQL) |
| `service` | Business rules in `@Transactional` methods: `StudentService`, `FacultyService`, `AdminService`, `AuthService` |
| `repository` | JDBC data access with parameterised SQL: `StudentRepository`, `FacultyRepository`, `AdminRepository`, `UserAccountRepository`, `CommonRepository` |
| `dto` | Request bodies with Bean Validation (e.g. marks ≤ 30/30/50, semester 1–10) |
| `model` | Row models; `Marks` holds the total-out-of-100 policy |
| `exception` | `NotFoundException` (404), `ForbiddenException` (403), `ConflictException` (409) |

Front end (`src/main/resources/static`): `index.html`, `css/portal.css`, and in `js/`: `api.js` (fetch client, error toasts, double-submit guard), `app.js` (navigation), `student.js`, `faculty.js`, `admin.js`, `admin-crud.js` (generic edit/delete, exam results, user accounts).

## Main endpoints

| Area | Endpoints |
|---|---|
| Auth | `POST /api/auth/login`, `GET /me`, `POST /logout`, `POST /provision` (admin) |
| Student | `GET profile, enrollments, offerings, attendance, marks, exams, results, re-exams` · `POST enroll` · `DELETE enrollments/{offeringId}` · `POST exams/register` · `DELETE exams/{examId}/registration` · `POST re-exams/apply` · `DELETE re-exams/{id}` |
| Faculty | `GET profile, offerings, offerings/{id}/students, exams` · `PUT offerings/{id}/classes-held, attendance, marks` |
| Admin | `GET stats` · `GET/POST/PUT/DELETE departments, programs, students, faculty, courses, offerings, exams` · `PATCH exams/{id}/status` · `GET exams/{id}/registrations` · `GET/POST/PUT/DELETE results` · `GET grades` · `GET re-exams`, `POST re-exams/decision` · `GET/PATCH/DELETE accounts` |

Creates return `201`, other successes `200`. Errors return `400` (validation), `401` (no session), `403` (role or ownership), `404` (unknown id) or `409` (duplicate, conflict, timeline, or a record still referenced by others). The full per-table matrix is in [`docs/phase3/CRUD_COVERAGE.md`](../docs/phase3/CRUD_COVERAGE.md).

## Configuration (`application.properties`)

* `spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1`, `username=campus_app`, `password=${CAMPUS_DB_PASSWORD:}`
* HikariCP: max pool 10, min idle 2, connection timeout 20 s
* `spring.web.resources.cache.cachecontrol.no-cache=true`, so browsers always revalidate the SPA files
