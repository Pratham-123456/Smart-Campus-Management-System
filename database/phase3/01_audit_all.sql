-- ==============================================================================
-- PHASE 3 AUDIT: 01_audit_all.sql  (Review 2, Appendix A Findings 2-8)
-- Read-only. Run BEFORE and AFTER 02_migrate_schema_and_data.sql.
-- Each query prints a count; after the migration every "violations" figure is 0.
-- Run 01_audit_row_counts.sql separately for Finding 1.
-- ==============================================================================
SET PAGESIZE 200 LINESIZE 200 FEEDBACK OFF
COLUMN check_name FORMAT A62
COLUMN detail FORMAT A60

PROMPT
PROMPT === Findings 2-8: summary of violations ===
SELECT 'F2  MARKS rows with a component above 30/30/50' AS check_name,
       COUNT(*) AS violations, 'ceilings are CA 30, Midterm 30, Final 50' AS detail
  FROM MARKS WHERE Continuous_Assessment_Marks > 30 OR Midterm_Marks > 30 OR Final_Exam_Marks > 50
UNION ALL
SELECT 'F2  (info) MARKS rows whose combined total exceeds 100', COUNT(*),
       'legitimate: combined maximum is 110'
  FROM MARKS WHERE Continuous_Assessment_Marks + Midterm_Marks + Final_Exam_Marks > 100
UNION ALL
SELECT 'F3  EXAM_RESULT grade not matching GRADE scale', COUNT(*),
       'grade = band of Marks_Obtained / Max_Marks * 100'
  FROM EXAM_RESULT r
  JOIN EXAM_REGISTRATION er ON er.Exam_Registration_ID = r.Exam_Registration_ID
  JOIN EXAM x ON x.Exam_ID = er.Exam_ID
 WHERE r.Grade_Code IS NOT NULL
   AND r.Grade_Code <> (SELECT g.Grade_Code FROM GRADE g
                         WHERE ROUND(r.Marks_Obtained / x.Max_Marks * 100, 2) BETWEEN g.Min_Percentage AND g.Max_Percentage)
UNION ALL
SELECT 'F4  students ENROLLED in 2+ sections of one course/term', COUNT(*),
       'groups (student, course, semester, year)'
  FROM (SELECT en.Student_ID FROM ENROLLMENT en JOIN COURSE_OFFERING co ON co.Offering_ID = en.Offering_ID
         WHERE en.Status = 'ENROLLED'
         GROUP BY en.Student_ID, co.Course_ID, co.Semester, co.Academic_Year HAVING COUNT(*) > 1)
UNION ALL
SELECT 'F5  overlapping exam registrations (same student)', COUNT(*),
       'pairs where startA < endB AND startB < endA'
  FROM EXAM_REGISTRATION a JOIN EXAM xa ON xa.Exam_ID = a.Exam_ID
  JOIN EXAM_REGISTRATION b ON b.Student_ID = a.Student_ID AND b.Exam_Registration_ID > a.Exam_Registration_ID
  JOIN EXAM xb ON xb.Exam_ID = b.Exam_ID
 WHERE a.Status = 'REGISTERED' AND b.Status = 'REGISTERED'
   AND xa.Exam_Date = xb.Exam_Date AND xa.Start_Time < xb.End_Time AND xb.Start_Time < xa.End_Time
UNION ALL
SELECT 'F6  future EXAM rows marked COMPLETED', COUNT(*), 'Exam_Date after today'
  FROM EXAM WHERE Status = 'COMPLETED' AND Exam_Date > TRUNC(SYSDATE)
UNION ALL
SELECT 'F6  EXAM_RESULT published before its exam date', COUNT(*), 'Published_Date < Exam_Date'
  FROM EXAM_RESULT r JOIN EXAM_REGISTRATION er ON er.Exam_Registration_ID = r.Exam_Registration_ID
  JOIN EXAM x ON x.Exam_ID = er.Exam_ID WHERE r.Published_Date < x.Exam_Date
UNION ALL
SELECT 'F6  EXAM_RESULT for an exam not yet held', COUNT(*), 'Exam_Date after today'
  FROM EXAM_RESULT r JOIN EXAM_REGISTRATION er ON er.Exam_Registration_ID = r.Exam_Registration_ID
  JOIN EXAM x ON x.Exam_ID = er.Exam_ID WHERE x.Exam_Date > TRUNC(SYSDATE)
UNION ALL
SELECT 'F6  EXAM_RESULT attached to an exam that is not COMPLETED', COUNT(*), 'results imply the exam was held'
  FROM EXAM_RESULT r JOIN EXAM_REGISTRATION er ON er.Exam_Registration_ID = r.Exam_Registration_ID
  JOIN EXAM x ON x.Exam_ID = er.Exam_ID WHERE x.Status <> 'COMPLETED'
UNION ALL
SELECT 'F6  active registration created after the exam ended', COUNT(*), 'Registration_Date > Exam_Date'
  FROM EXAM_REGISTRATION er JOIN EXAM x ON x.Exam_ID = er.Exam_ID
 WHERE er.Status = 'REGISTERED' AND er.Registration_Date > x.Exam_Date
UNION ALL
SELECT 'F6  re-exam application dated before result publication', COUNT(*), 'Application_Date < Published_Date'
  FROM RE_EXAM_APPLICATION a JOIN EXAM_RESULT r ON r.Exam_Result_ID = a.Exam_Result_ID
 WHERE a.Application_Date < r.Published_Date
UNION ALL
SELECT 'F7  COURSE_OFFERING.Semester precision below 2', COUNT(*), 'NUMBER(1) cannot store semester 10'
  FROM USER_TAB_COLUMNS WHERE Table_Name = 'COURSE_OFFERING' AND Column_Name = 'SEMESTER' AND Data_Precision < 2
UNION ALL
SELECT 'F8  status columns without a named CHECK constraint', 5 - COUNT(*), 'expects 5 CK_*_STATUS constraints'
  FROM USER_CONSTRAINTS WHERE Constraint_Name IN
       ('CK_ENROLLMENT_STATUS', 'CK_EXAM_STATUS', 'CK_EXAMREG_STATUS', 'CK_RESULT_STATUS', 'CK_REEXAM_STATUS')
UNION ALL
SELECT 'F2  MARKS ceiling CHECK constraints missing', 3 - COUNT(*), 'CK_MARKS_CA / _MIDTERM / _FINAL'
  FROM USER_CONSTRAINTS WHERE Constraint_Name IN ('CK_MARKS_CA', 'CK_MARKS_MIDTERM', 'CK_MARKS_FINAL');

PROMPT
PROMPT === Finding 3: the three Review 2 seed grades (expected B, A, D) ===
SELECT r.Exam_Result_ID, r.Marks_Obtained, x.Max_Marks,
       ROUND(r.Marks_Obtained / x.Max_Marks * 100, 2) AS pct, r.Grade_Code AS stored_grade,
       (SELECT g.Grade_Code FROM GRADE g
         WHERE ROUND(r.Marks_Obtained / x.Max_Marks * 100, 2) BETWEEN g.Min_Percentage AND g.Max_Percentage) AS scale_grade
  FROM EXAM_RESULT r JOIN EXAM_REGISTRATION er ON er.Exam_Registration_ID = r.Exam_Registration_ID
  JOIN EXAM x ON x.Exam_ID = er.Exam_ID
 WHERE r.Exam_Result_ID IN (10002, 10003, 10004) ORDER BY 1;

PROMPT
PROMPT === Finding 8: distinct values currently stored in the five status columns ===
SELECT 'ENROLLMENT.Status' AS check_name, Status AS detail, COUNT(*) AS violations FROM ENROLLMENT GROUP BY Status
UNION ALL SELECT 'EXAM.Status', Status, COUNT(*) FROM EXAM GROUP BY Status
UNION ALL SELECT 'EXAM_REGISTRATION.Status', Status, COUNT(*) FROM EXAM_REGISTRATION GROUP BY Status
UNION ALL SELECT 'EXAM_RESULT.Result_Status', Result_Status, COUNT(*) FROM EXAM_RESULT GROUP BY Result_Status
UNION ALL SELECT 'RE_EXAM_APPLICATION.Status', Status, COUNT(*) FROM RE_EXAM_APPLICATION GROUP BY Status
ORDER BY 1, 2;
