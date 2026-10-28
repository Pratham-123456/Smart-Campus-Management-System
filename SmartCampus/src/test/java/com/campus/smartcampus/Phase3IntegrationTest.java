package com.campus.smartcampus;

import com.campus.smartcampus.dto.LoginResponse;
import com.campus.smartcampus.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Phase 3 end-to-end tests: real controllers, services, JDBC repositories and the Oracle
 * dev database (FREEPDB1). Every test runs in a transaction that is rolled back, and all
 * fixtures use IDs from 990001 upward, so the live data is never changed.
 *
 * Requires the CAMPUS_DB_PASSWORD environment variable (same as the application).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "CAMPUS_DB_PASSWORD", matches = ".+")
class Phase3IntegrationTest {

    // Fixture IDs
    static final long DEPT = 990001, PROGRAM = 990001;
    static final long COURSE_1 = 990001, COURSE_2 = 990002;
    static final long FAC_1 = 990001, FAC_2 = 990002;
    static final long STU_1 = 990001, STU_2 = 990002, STU_3 = 990003;
    static final long OFF_1A = 990001;  // course 1, section A, 2 seats
    static final long OFF_1B = 990002;  // course 1, section B (same term)
    static final long OFF_2A = 990003;  // course 2, section A
    static final long EXAM_A = 990001;  // OFF_1A, 2099-01-10 09:00-10:30
    static final long EXAM_B = 990002;  // OFF_2A, 2099-01-10 10:00-11:30 (overlaps A)
    static final long EXAM_C = 990003;  // OFF_2A, 2099-01-10 10:30-12:00 (back-to-back with A)
    static final long EXAM_D = 990004;  // OFF_2A, 2099-01-11 09:00-10:30 (different day)
    static final long EXAM_PAST = 990005; // OFF_1A, 2020-01-10, COMPLETED, max 50
    static final long REG_PAST = 990001;  // STU_1 -> EXAM_PAST

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    MockHttpSession admin, student1, student2, student3, faculty1, faculty2;

    @BeforeEach
    void fixtures() {
        jdbc.update("INSERT INTO DEPARTMENT VALUES (?, 'P3 Test Department', 'TEST-1')", DEPT);
        jdbc.update("INSERT INTO PROGRAM VALUES (?, 'P3 Test Program', 'B.Tech', 4, ?)", PROGRAM, DEPT);
        jdbc.update("INSERT INTO COURSE VALUES (?, 'P3 Test Course One', 4, 'Core', ?)", COURSE_1, DEPT);
        jdbc.update("INSERT INTO COURSE VALUES (?, 'P3 Test Course Two', 3, 'Core', ?)", COURSE_2, DEPT);
        jdbc.update("INSERT INTO FACULTY VALUES (?, 'P3 Faculty One', 'p3.f1@test.local', NULL, 'Professor', ?)", FAC_1, DEPT);
        jdbc.update("INSERT INTO FACULTY VALUES (?, 'P3 Faculty Two', 'p3.f2@test.local', NULL, 'Professor', ?)", FAC_2, DEPT);
        for (long s : new long[]{STU_1, STU_2, STU_3}) {
            jdbc.update("INSERT INTO STUDENT VALUES (?, ?, ?, NULL, NULL, 2025, ?)", s, "P3 Student " + s, "p3." + s + "@test.local", PROGRAM);
        }
        jdbc.update("INSERT INTO COURSE_OFFERING VALUES (?, ?, ?, 3, '2098-2099', 'A', 'T-1', 2, 10)", OFF_1A, COURSE_1, FAC_1);
        jdbc.update("INSERT INTO COURSE_OFFERING VALUES (?, ?, ?, 3, '2098-2099', 'B', 'T-2', 30, 10)", OFF_1B, COURSE_1, FAC_2);
        jdbc.update("INSERT INTO COURSE_OFFERING VALUES (?, ?, ?, 3, '2098-2099', 'A', 'T-3', 30, 10)", OFF_2A, COURSE_2, FAC_1);
        enrollDirect(990001, STU_1, OFF_1A);
        enrollDirect(990002, STU_1, OFF_2A);

        exam(EXAM_A, OFF_1A, "2099-01-10", "09:00", "10:30", "SCHEDULED");
        exam(EXAM_B, OFF_2A, "2099-01-10", "10:00", "11:30", "SCHEDULED");
        exam(EXAM_C, OFF_2A, "2099-01-10", "10:30", "12:00", "SCHEDULED");
        exam(EXAM_D, OFF_2A, "2099-01-11", "09:00", "10:30", "SCHEDULED");
        exam(EXAM_PAST, OFF_1A, "2020-01-10", "09:00", "10:30", "COMPLETED");
        jdbc.update("INSERT INTO EXAM_REGISTRATION VALUES (?, ?, ?, DATE '2020-01-01', 'REGISTERED')", REG_PAST, STU_1, EXAM_PAST);

        admin = session("ADMINISTRATOR", null);
        student1 = session("STUDENT", STU_1);
        student2 = session("STUDENT", STU_2);
        student3 = session("STUDENT", STU_3);
        faculty1 = session("FACULTY", FAC_1);
        faculty2 = session("FACULTY", FAC_2);
    }

    // ------------------------------------------------------------------ helpers

    void enrollDirect(long id, long student, long offering) {
        jdbc.update("INSERT INTO ENROLLMENT VALUES (?, ?, ?, SYSDATE, 'ENROLLED')", id, student, offering);
        jdbc.update("INSERT INTO ATTENDANCE VALUES (?, ?, ?, 0)", id, student, offering);
        jdbc.update("INSERT INTO MARKS VALUES (?, ?, ?, 0, 0, 0)", id, student, offering);
    }

    void exam(long id, long offering, String date, String start, String end, String status) {
        jdbc.update("INSERT INTO EXAM VALUES (?, ?, 'CAT1', TO_DATE(?, 'YYYY-MM-DD'), TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI'), "
                + "TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI'), 'TEST HALL', 50, ?)",
                id, offering, date, date + " " + start, date + " " + end, status);
    }

    static MockHttpSession session(String role, Long id) {
        LoginResponse user = new LoginResponse();
        user.setRole(role);
        user.setId(id);
        user.setName("test " + role);
        MockHttpSession s = new MockHttpSession();
        s.setAttribute(AuthService.SESSION_AUTH_KEY, user);
        return s;
    }

    ResultActions post(MockHttpSession s, String url, String json) throws Exception {
        return mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post(url)
                .session(s).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    ResultActions putJson(MockHttpSession s, String url, String json) throws Exception {
        return mvc.perform(put(url).session(s).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    ResultActions patchJson(MockHttpSession s, String url, String json) throws Exception {
        return mvc.perform(patch(url).session(s).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    ResultActions del(MockHttpSession s, String url) throws Exception {
        return mvc.perform(delete(url).session(s));
    }

    ResultActions getAs(MockHttpSession s, String url) throws Exception {
        return mvc.perform(get(url).session(s));
    }

    String enrollmentStatus(long student, long offering) {
        return jdbc.queryForObject("SELECT Status FROM ENROLLMENT WHERE Student_ID = ? AND Offering_ID = ?", String.class, student, offering);
    }

    // ================================================================== Course registration

    @Nested
    @DisplayName("Course registration (Finding 4)")
    class CourseRegistration {
        @Test
        void validEnrollmentReturns201() throws Exception {
            post(student2, "/api/student/" + STU_2 + "/enroll", "{\"offeringId\": " + OFF_1A + "}")
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.enrollmentId").isNumber());
            assertThat(enrollmentStatus(STU_2, OFF_1A)).isEqualTo("ENROLLED");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM ATTENDANCE WHERE Student_ID = ? AND Offering_ID = ?", Integer.class, STU_2, OFF_1A)).isEqualTo(1);
        }

        @Test
        void sameOfferingTwiceReturns409() throws Exception {
            post(student1, "/api/student/" + STU_1 + "/enroll", "{\"offeringId\": " + OFF_1A + "}")
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already enrolled")));
        }

        @Test
        void otherSectionOfSameCourseSameTermReturns409() throws Exception {
            post(student1, "/api/student/" + STU_1 + "/enroll", "{\"offeringId\": " + OFF_1B + "}")
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("another section")));
        }

        @Test
        void fullOfferingReturns409() throws Exception {
            post(student2, "/api/student/" + STU_2 + "/enroll", "{\"offeringId\": " + OFF_1A + "}").andExpect(status().isCreated());
            post(student3, "/api/student/" + STU_3 + "/enroll", "{\"offeringId\": " + OFF_1A + "}")
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("full capacity")));
        }

        @Test
        void unknownOfferingReturns404() throws Exception {
            post(student2, "/api/student/" + STU_2 + "/enroll", "{\"offeringId\": 99999999}")
                    .andExpect(status().isNotFound());
        }

        @Test
        void dropIsLogicalAndAllowsOtherSectionAfterwards() throws Exception {
            del(student1, "/api/student/" + STU_1 + "/enrollments/" + OFF_1A).andExpect(status().isOk());
            assertThat(enrollmentStatus(STU_1, OFF_1A)).isEqualTo("DROPPED");
            // history row still exists (no hard delete)
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM MARKS WHERE Student_ID = ? AND Offering_ID = ?", Integer.class, STU_1, OFF_1A)).isEqualTo(1);
            post(student1, "/api/student/" + STU_1 + "/enroll", "{\"offeringId\": " + OFF_1B + "}").andExpect(status().isCreated());
            del(student1, "/api/student/" + STU_1 + "/enrollments/" + OFF_1A).andExpect(status().isNotFound());
        }

        @Test
        void cannotDropCourseWithPublishedResult() throws Exception {
            jdbc.update("INSERT INTO EXAM_RESULT VALUES (990001, ?, 40, 'PASS', 'A', SYSDATE)", REG_PAST);
            del(student1, "/api/student/" + STU_1 + "/enrollments/" + OFF_1A).andExpect(status().isConflict());
        }
    }

    // ================================================================== Exam registration

    @Nested
    @DisplayName("Exam registration (Finding 5)")
    class ExamRegistration {
        String register(long exam) {
            return "{\"examId\": " + exam + "}";
        }

        @Test
        void validDuplicateOverlapAdjacentAndOtherDay() throws Exception {
            String url = "/api/student/" + STU_1 + "/exams/register";
            post(student1, url, register(EXAM_A)).andExpect(status().isCreated());
            post(student1, url, register(EXAM_A)).andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already registered")));
            post(student1, url, register(EXAM_B)).andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("clash")));
            post(student1, url, register(EXAM_C)).andExpect(status().isCreated()); // starts exactly when A ends
            post(student1, url, register(EXAM_D)).andExpect(status().isCreated()); // different date
        }

        @Test
        void unknownExamReturns404() throws Exception {
            post(student1, "/api/student/" + STU_1 + "/exams/register", register(99999999)).andExpect(status().isNotFound());
        }

        @Test
        void notEnrolledReturns409() throws Exception {
            post(student2, "/api/student/" + STU_2 + "/exams/register", register(EXAM_A)).andExpect(status().isConflict());
        }

        @Test
        void completedExamIsClosedForRegistration() throws Exception {
            post(student1, "/api/student/" + STU_1 + "/exams/register", register(EXAM_PAST)).andExpect(status().isConflict());
        }

        @Test
        void cancelThenReRegisterReusesRow() throws Exception {
            String url = "/api/student/" + STU_1 + "/exams/register";
            post(student1, url, register(EXAM_A)).andExpect(status().isCreated());
            del(student1, "/api/student/" + STU_1 + "/exams/" + EXAM_A + "/registration").andExpect(status().isOk());
            // after cancellation the overlapping exam B is allowed
            post(student1, url, register(EXAM_B)).andExpect(status().isCreated());
            post(student1, url, register(EXAM_A)).andExpect(status().isConflict()); // now clashes with B
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM EXAM_REGISTRATION WHERE Student_ID = ? AND Exam_ID = ?", Integer.class, STU_1, EXAM_A)).isEqualTo(1);
        }
    }

    // ================================================================== Faculty: attendance & marks

    @Nested
    @DisplayName("Faculty attendance and marks (Finding 2)")
    class FacultyUpdates {
        String attendance(long attended) {
            return "{\"studentId\": " + STU_1 + ", \"offeringId\": " + OFF_1A + ", \"classesAttended\": " + attended + "}";
        }

        String marks(double ca, double mid, double fin) {
            return "{\"studentId\": " + STU_1 + ", \"offeringId\": " + OFF_1A + ", \"continuousAssessmentMarks\": " + ca
                    + ", \"midtermMarks\": " + mid + ", \"finalExamMarks\": " + fin + "}";
        }

        @Test
        void attendanceValidNegativeAndAboveHeld() throws Exception {
            String url = "/api/faculty/" + FAC_1 + "/attendance";
            putJson(faculty1, url, attendance(10)).andExpect(status().isOk());
            putJson(faculty1, url, attendance(-1)).andExpect(status().isBadRequest());
            putJson(faculty1, url, attendance(11)).andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("cannot exceed")));
        }

        @Test
        void classesHeldCannotDropBelowRecordedAttendance() throws Exception {
            putJson(faculty1, "/api/faculty/" + FAC_1 + "/attendance", attendance(8)).andExpect(status().isOk());
            putJson(faculty1, "/api/faculty/" + FAC_1 + "/offerings/" + OFF_1A + "/classes-held", "{\"classesHeld\": 7}")
                    .andExpect(status().isBadRequest());
            putJson(faculty1, "/api/faculty/" + FAC_1 + "/offerings/" + OFF_1A + "/classes-held", "{\"classesHeld\": 12}")
                    .andExpect(status().isOk());
        }

        @Test
        void wrongFacultyGets403() throws Exception {
            // FAC_2 is authenticated as itself but OFF_1A belongs to FAC_1 (service-level check)
            putJson(faculty2, "/api/faculty/" + FAC_2 + "/attendance", attendance(5)).andExpect(status().isForbidden());
            // FAC_2 addressing FAC_1's URL space (interceptor-level check)
            getAs(faculty2, "/api/faculty/" + FAC_1 + "/offerings").andExpect(status().isForbidden());
        }

        @Test
        void marksBoundary303050IsAcceptedAndTotalIs100() throws Exception {
            post(faculty1, "/api/faculty/" + FAC_1 + "/marks", marks(30, 30, 50)).andExpect(status().isOk());
            getAs(student1, "/api/student/" + STU_1 + "/marks")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[?(@.offeringId == " + OFF_1A + ")].rawTotal").value(110.0))
                    .andExpect(jsonPath("$.data[?(@.offeringId == " + OFF_1A + ")].totalMarks").value(100.0))
                    .andExpect(jsonPath("$.data[?(@.offeringId == " + OFF_1A + ")].maxTotal").value(100.0))
                    .andExpect(jsonPath("$.data[?(@.offeringId == " + OFF_1A + ")].gradeCode").value("S"));
            // faculty roster reports the same /100 total
            getAs(faculty1, "/api/faculty/" + FAC_1 + "/offerings/" + OFF_1A + "/students")
                    .andExpect(jsonPath("$.data[?(@.studentId == " + STU_1 + ")].totalMarks").value(100.0));
        }

        @Test
        void rawSumAbove100IsScaledSoTotalStaysWithin100() throws Exception {
            // 28 + 27 + 47 = 102 of 110 -> total 92.73 / 100 -> S
            post(faculty1, "/api/faculty/" + FAC_1 + "/marks", marks(28, 27, 47)).andExpect(status().isOk());
            getAs(student1, "/api/student/" + STU_1 + "/marks")
                    .andExpect(jsonPath("$.data[?(@.offeringId == " + OFF_1A + ")].rawTotal").value(102.0))
                    .andExpect(jsonPath("$.data[?(@.offeringId == " + OFF_1A + ")].totalMarks").value(92.73));
            // 20 + 20 + 37 = 77 / 110 = 70.0 % -> B (it would be C if read as 77 %)
            post(faculty1, "/api/faculty/" + FAC_1 + "/marks", marks(20, 20, 37)).andExpect(status().isOk());
            getAs(student1, "/api/student/" + STU_1 + "/marks")
                    .andExpect(jsonPath("$.data[?(@.offeringId == " + OFF_1A + ")].gradeCode").value("B"));
        }

        @Test
        void marksAboveCeilingsOrNegativeAre400() throws Exception {
            String url = "/api/faculty/" + FAC_1 + "/marks";
            post(faculty1, url, marks(31, 0, 0)).andExpect(status().isBadRequest());
            post(faculty1, url, marks(0, 30.5, 0)).andExpect(status().isBadRequest());
            post(faculty1, url, marks(0, 0, 51)).andExpect(status().isBadRequest());
            post(faculty1, url, marks(-1, 0, 0)).andExpect(status().isBadRequest());
        }
    }

    // ================================================================== Results & re-exams

    @Nested
    @DisplayName("Results and re-exams (Findings 3, 6)")
    class ResultsAndReExams {
        @Test
        void seedGradesFixedByMigration() {
            assertThat(jdbc.queryForObject("SELECT Grade_Code FROM EXAM_RESULT WHERE Exam_Result_ID = 10002", String.class)).isEqualTo("B");
            assertThat(jdbc.queryForObject("SELECT Grade_Code FROM EXAM_RESULT WHERE Exam_Result_ID = 10003", String.class)).isEqualTo("A");
            assertThat(jdbc.queryForObject("SELECT Grade_Code FROM EXAM_RESULT WHERE Exam_Result_ID = 10004", String.class)).isEqualTo("D");
        }

        @Test
        void noLiveExamOrResultViolatesTheTimeline() {
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM EXAM WHERE Status = 'COMPLETED' AND Exam_Date > TRUNC(SYSDATE)", Integer.class)).isZero();
            assertThat(jdbc.queryForObject("""
                    SELECT COUNT(*) FROM EXAM_RESULT r JOIN EXAM_REGISTRATION er ON er.Exam_Registration_ID = r.Exam_Registration_ID
                    JOIN EXAM x ON x.Exam_ID = er.Exam_ID WHERE r.Published_Date < x.Exam_Date OR x.Exam_Date > TRUNC(SYSDATE)
                    """, Integer.class)).isZero();
        }

        @Test
        void publishAboveMaxMarksIs400AndValidPublishDerivesGrade() throws Exception {
            post(admin, "/api/admin/results", "{\"examRegistrationId\": " + REG_PAST + ", \"marksObtained\": 60}")
                    .andExpect(status().isBadRequest());
            post(admin, "/api/admin/results", "{\"examRegistrationId\": " + REG_PAST + ", \"marksObtained\": 40}")
                    .andExpect(status().isCreated());
            assertThat(jdbc.queryForObject("SELECT Grade_Code || ':' || Result_Status FROM EXAM_RESULT WHERE Exam_Registration_ID = ?", String.class, REG_PAST))
                    .isEqualTo("A:PASS"); // 40 / 50 = 80 %
            post(admin, "/api/admin/results", "{\"examRegistrationId\": " + REG_PAST + ", \"marksObtained\": 30}")
                    .andExpect(status().isConflict()); // already published
        }

        @Test
        void futureExamCannotBeCompletedOrHaveResults() throws Exception {
            jdbc.update("INSERT INTO EXAM_REGISTRATION VALUES (990002, ?, ?, SYSDATE, 'REGISTERED')", STU_1, EXAM_A);
            patchJson(admin, "/api/admin/exams/" + EXAM_A + "/status", "{\"status\": \"COMPLETED\"}")
                    .andExpect(status().isConflict());
            post(admin, "/api/admin/results", "{\"examRegistrationId\": 990002, \"marksObtained\": 30}")
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("future exam")));
        }

        @Test
        void reExamPendingToApprovedThenLocked() throws Exception {
            jdbc.update("INSERT INTO EXAM_RESULT VALUES (990001, ?, 20, 'FAIL', 'F', SYSDATE)", REG_PAST);
            String apply = "{\"examResultId\": 990001, \"reason\": \"Paper re-evaluation requested\"}";
            post(student1, "/api/student/" + STU_1 + "/re-exams/apply", apply).andExpect(status().isCreated());
            post(student1, "/api/student/" + STU_1 + "/re-exams/apply", apply).andExpect(status().isConflict());
            Long appId = jdbc.queryForObject("SELECT ReExam_Application_ID FROM RE_EXAM_APPLICATION WHERE Exam_Result_ID = 990001", Long.class);

            // other students and non-admins cannot act on it
            post(student2, "/api/student/" + STU_2 + "/re-exams/apply", apply).andExpect(status().isForbidden());
            post(student1, "/api/admin/re-exams/decision", "{\"applicationId\": " + appId + ", \"decision\": \"APPROVED\"}")
                    .andExpect(status().isForbidden());

            post(admin, "/api/admin/re-exams/decision", "{\"applicationId\": " + appId + ", \"decision\": \"APPROVED\"}")
                    .andExpect(status().isOk());
            assertThat(jdbc.queryForObject("SELECT Status FROM RE_EXAM_APPLICATION WHERE ReExam_Application_ID = ?", String.class, appId)).isEqualTo("APPROVED");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM RE_EXAM_APPLICATION WHERE ReExam_Application_ID = ? AND Approval_Date IS NOT NULL", Integer.class, appId)).isEqualTo(1);

            post(admin, "/api/admin/re-exams/decision", "{\"applicationId\": " + appId + ", \"decision\": \"REJECTED\"}")
                    .andExpect(status().isConflict());
            del(student1, "/api/student/" + STU_1 + "/re-exams/" + appId).andExpect(status().isConflict());
            post(admin, "/api/admin/re-exams/decision", "{\"applicationId\": 99999999, \"decision\": \"REJECTED\"}")
                    .andExpect(status().isNotFound());
        }

        @Test
        void pendingReExamCanBeWithdrawn() throws Exception {
            jdbc.update("INSERT INTO EXAM_RESULT VALUES (990001, ?, 20, 'FAIL', 'F', SYSDATE)", REG_PAST);
            post(student1, "/api/student/" + STU_1 + "/re-exams/apply", "{\"examResultId\": 990001, \"reason\": \"Totalling error\"}")
                    .andExpect(status().isCreated());
            Long appId = jdbc.queryForObject("SELECT ReExam_Application_ID FROM RE_EXAM_APPLICATION WHERE Exam_Result_ID = 990001", Long.class);
            del(student1, "/api/student/" + STU_1 + "/re-exams/" + appId).andExpect(status().isOk());
            // a result with no application can be deleted; with one it cannot
            del(admin, "/api/admin/results/990001").andExpect(status().isOk());
        }
    }

    // ================================================================== Schema rules

    @Nested
    @DisplayName("Schema constraints (Findings 7, 8)")
    class SchemaRules {
        String offering(int semester, String section) {
            return "{\"courseId\": " + COURSE_2 + ", \"facultyId\": " + FAC_1 + ", \"semester\": " + semester
                    + ", \"academicYear\": \"2098-2099\", \"section\": \"" + section + "\", \"roomNo\": \"T\", \"maxSeats\": 10, \"classesHeld\": 0}";
        }

        @Test
        void semesterOneAndTenAcceptedZeroAndElevenRejected() throws Exception {
            post(admin, "/api/admin/offerings", offering(1, "S1")).andExpect(status().isCreated());
            post(admin, "/api/admin/offerings", offering(10, "S10")).andExpect(status().isCreated());
            post(admin, "/api/admin/offerings", offering(0, "S0")).andExpect(status().isBadRequest());
            post(admin, "/api/admin/offerings", offering(11, "S11")).andExpect(status().isBadRequest());
            assertThat(jdbc.queryForObject("SELECT MAX(Semester) FROM COURSE_OFFERING WHERE Section = 'S10' AND Course_ID = ?", Integer.class, COURSE_2)).isEqualTo(10);
            // Oracle itself rejects 11 (CHECK) even if the API were bypassed
            assertThatThrownBy(() -> jdbc.update("UPDATE COURSE_OFFERING SET Semester = 11 WHERE Offering_ID = ?", OFF_2A))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void allFiveStatusChecksAndMarksCeilingsEnforcedByOracle() {
            String[] bad = {
                    "UPDATE ENROLLMENT SET Status = 'BOGUS' WHERE Enrollment_ID = 990001",
                    "UPDATE EXAM SET Status = 'DONE' WHERE Exam_ID = 990001",
                    "UPDATE EXAM_REGISTRATION SET Status = 'MAYBE' WHERE Exam_Registration_ID = 990001",
                    "INSERT INTO EXAM_RESULT VALUES (990009, 990001, 10, 'PUBLISHED', 'F', SYSDATE)",
                    "UPDATE MARKS SET Final_Exam_Marks = 51 WHERE Mark_ID = 990001"
            };
            for (String sql : bad) {
                assertThatThrownBy(() -> jdbc.update(sql)).as(sql).isInstanceOf(DataIntegrityViolationException.class);
            }
            jdbc.update("INSERT INTO EXAM_RESULT VALUES (990009, 990001, 10, 'FAIL', 'F', SYSDATE)");
            jdbc.update("INSERT INTO RE_EXAM_APPLICATION VALUES (990009, 990009, SYSDATE, 'probe', 'PENDING', NULL)");
            assertThatThrownBy(() -> jdbc.update("UPDATE RE_EXAM_APPLICATION SET Status = 'ON_HOLD' WHERE ReExam_Application_ID = 990009"))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    // ================================================================== CRUD + FK protection + auth

    @Nested
    @DisplayName("Admin CRUD, FK protection and access control")
    class CrudAndAccess {
        @Test
        void departmentLifecycle() throws Exception {
            post(admin, "/api/admin/departments", "{\"departmentName\": \"P3 Lifecycle Dept\", \"officeLocation\": \"L-1\"}")
                    .andExpect(status().isCreated());
            Long id = jdbc.queryForObject("SELECT Department_ID FROM DEPARTMENT WHERE Department_Name = 'P3 Lifecycle Dept'", Long.class);
            putJson(admin, "/api/admin/departments/" + id, "{\"departmentName\": \"P3 Lifecycle Dept 2\", \"officeLocation\": \"L-2\"}")
                    .andExpect(status().isOk());
            getAs(admin, "/api/admin/departments")
                    .andExpect(jsonPath("$.data[?(@.departmentId == " + id + ")].officeLocation").value("L-2"));
            del(admin, "/api/admin/departments/" + id).andExpect(status().isOk());
            del(admin, "/api/admin/departments/" + id).andExpect(status().isNotFound());
            putJson(admin, "/api/admin/departments/" + id, "{\"departmentName\": \"x\"}").andExpect(status().isNotFound());
        }

        @Test
        void parentsWithChildrenReturn409() throws Exception {
            del(admin, "/api/admin/departments/" + DEPT).andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("never cascade-deleted")));
            del(admin, "/api/admin/programs/" + PROGRAM).andExpect(status().isConflict());
            del(admin, "/api/admin/courses/" + COURSE_1).andExpect(status().isConflict());
            del(admin, "/api/admin/faculty/" + FAC_1).andExpect(status().isConflict());
            del(admin, "/api/admin/students/" + STU_1).andExpect(status().isConflict());
            del(admin, "/api/admin/offerings/" + OFF_1A).andExpect(status().isConflict());
            del(admin, "/api/admin/exams/" + EXAM_PAST).andExpect(status().isConflict());
        }

        @Test
        void unreferencedRecordsCanBeUpdatedAndDeleted() throws Exception {
            putJson(admin, "/api/admin/students/" + STU_3, "{\"name\": \"P3 Renamed\", \"email\": \"p3.renamed@test.local\", \"admissionYear\": 2025, \"programId\": " + PROGRAM + "}")
                    .andExpect(status().isOk());
            del(admin, "/api/admin/students/" + STU_3).andExpect(status().isOk());
            del(admin, "/api/admin/exams/" + EXAM_D).andExpect(status().isOk());
            del(admin, "/api/admin/offerings/" + OFF_1B).andExpect(status().isOk());
            del(admin, "/api/admin/faculty/" + FAC_2).andExpect(status().isOk());
            patchJson(admin, "/api/admin/exams/" + EXAM_C + "/status", "{\"status\": \"CANCELLED\"}").andExpect(status().isOk());
        }

        @Test
        void offeringSeatsCannotGoBelowEnrolled() throws Exception {
            String body = "{\"courseId\": " + COURSE_1 + ", \"facultyId\": " + FAC_1 + ", \"semester\": 3, \"academicYear\": \"2098-2099\", "
                    + "\"section\": \"A\", \"roomNo\": \"T-1\", \"maxSeats\": 0, \"classesHeld\": 10}";
            putJson(admin, "/api/admin/offerings/" + OFF_1A, body).andExpect(status().isBadRequest()); // @Min(1)
            post(student2, "/api/student/" + STU_2 + "/enroll", "{\"offeringId\": " + OFF_1A + "}").andExpect(status().isCreated());
            putJson(admin, "/api/admin/offerings/" + OFF_1A, body.replace("\"maxSeats\": 0", "\"maxSeats\": 1")).andExpect(status().isConflict());
        }

        @Test
        void unauthenticatedRequestsGet401() throws Exception {
            mvc.perform(get("/api/admin/stats")).andExpect(status().isUnauthorized());
            mvc.perform(get("/api/student/" + STU_1 + "/marks")).andExpect(status().isUnauthorized());
        }

        @Test
        void crossStudentAndNonAdminGet403() throws Exception {
            getAs(student1, "/api/student/" + STU_2 + "/profile").andExpect(status().isForbidden());
            getAs(student1, "/api/admin/stats").andExpect(status().isForbidden());
            getAs(faculty1, "/api/admin/departments").andExpect(status().isForbidden());
            del(student1, "/api/admin/departments/" + DEPT).andExpect(status().isForbidden());
        }

        @Test
        void errorsNeverLeakSql() throws Exception {
            post(admin, "/api/admin/departments", "{\"departmentName\": \"P3 Test Department\"}") // UNIQUE name
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("ORA-"))));
        }
    }
}
