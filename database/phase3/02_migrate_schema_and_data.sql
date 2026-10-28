-- ==============================================================================
-- PHASE 3 MIGRATION: 02_migrate_schema_and_data.sql
-- Applies the Review 2 Appendix A fixes to an ALREADY POPULATED database.
--
-- SAFETY
--   * Idempotent: every step checks current state first, so re-running is a no-op.
--   * No DROP / TRUNCATE / DELETE. Rows are corrected in place; history is kept.
--   * Run 01_audit_all.sql before and 03_verify_post_migration.sql after.
--   * Fresh installs get the same end state from sql/01..04 (already updated).
-- ==============================================================================
SET SERVEROUTPUT ON SIZE UNLIMITED
SET DEFINE OFF
WHENEVER SQLERROR EXIT FAILURE ROLLBACK

PROMPT [F7] Widening COURSE_OFFERING.Semester to NUMBER(2)...
DECLARE
    v_precision NUMBER;
BEGIN
    SELECT Data_Precision INTO v_precision FROM USER_TAB_COLUMNS
     WHERE Table_Name = 'COURSE_OFFERING' AND Column_Name = 'SEMESTER';
    IF v_precision < 2 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE COURSE_OFFERING MODIFY (Semester NUMBER(2))';
        DBMS_OUTPUT.PUT_LINE('  Semester widened from NUMBER(' || v_precision || ') to NUMBER(2).');
    ELSE
        DBMS_OUTPUT.PUT_LINE('  Already NUMBER(' || v_precision || '); nothing to do.');
    END IF;
END;
/

PROMPT [F3] Correcting the three off-scale seed grades (10002=B, 10003=A, 10004=D)...
UPDATE EXAM_RESULT SET Grade_Code = 'B' WHERE Exam_Result_ID = 10002 AND Grade_Code <> 'B';
UPDATE EXAM_RESULT SET Grade_Code = 'A' WHERE Exam_Result_ID = 10003 AND Grade_Code <> 'A';
UPDATE EXAM_RESULT SET Grade_Code = 'D' WHERE Exam_Result_ID = 10004 AND Grade_Code <> 'D';

PROMPT [F5][F6] Re-timetabling the 42 seed exams into unique, non-overlapping slots...
-- CAT1 07-12 Sep, CAT2 28 Sep-02 Oct (3 slots/day), the three FATs that already have
-- published results on 03-04 Oct, and the remaining FATs 16-19 Nov (2 slots/day).
-- Every exam then occupies its own slot, so no student can hold two clashing
-- registrations, every published result (05 Oct or earlier) follows its exam, and
-- only exams dated before today keep Status = COMPLETED.
MERGE INTO EXAM x
USING (
    SELECT 801 AS Exam_ID, DATE '2026-09-07' AS New_Date, TIMESTAMP '2026-09-07 09:00:00' AS New_Start, TIMESTAMP '2026-09-07 10:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 802 AS Exam_ID, DATE '2026-09-07' AS New_Date, TIMESTAMP '2026-09-07 11:30:00' AS New_Start, TIMESTAMP '2026-09-07 12:30:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 803 AS Exam_ID, DATE '2026-09-07' AS New_Date, TIMESTAMP '2026-09-07 14:30:00' AS New_Start, TIMESTAMP '2026-09-07 15:30:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 804 AS Exam_ID, DATE '2026-09-28' AS New_Date, TIMESTAMP '2026-09-28 09:00:00' AS New_Start, TIMESTAMP '2026-09-28 10:30:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 805 AS Exam_ID, DATE '2026-09-08' AS New_Date, TIMESTAMP '2026-09-08 09:00:00' AS New_Start, TIMESTAMP '2026-09-08 10:30:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 806 AS Exam_ID, DATE '2026-09-28' AS New_Date, TIMESTAMP '2026-09-28 11:30:00' AS New_Start, TIMESTAMP '2026-09-28 13:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 807 AS Exam_ID, DATE '2026-09-08' AS New_Date, TIMESTAMP '2026-09-08 11:30:00' AS New_Start, TIMESTAMP '2026-09-08 13:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 808 AS Exam_ID, DATE '2026-09-28' AS New_Date, TIMESTAMP '2026-09-28 14:30:00' AS New_Start, TIMESTAMP '2026-09-28 16:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 809 AS Exam_ID, DATE '2026-09-29' AS New_Date, TIMESTAMP '2026-09-29 09:00:00' AS New_Start, TIMESTAMP '2026-09-29 10:30:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 810 AS Exam_ID, DATE '2026-10-03' AS New_Date, TIMESTAMP '2026-10-03 09:00:00' AS New_Start, TIMESTAMP '2026-10-03 12:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 811 AS Exam_ID, DATE '2026-10-03' AS New_Date, TIMESTAMP '2026-10-03 14:00:00' AS New_Start, TIMESTAMP '2026-10-03 17:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 812 AS Exam_ID, DATE '2026-11-16' AS New_Date, TIMESTAMP '2026-11-16 09:00:00' AS New_Start, TIMESTAMP '2026-11-16 12:00:00' AS New_End, 'SCHEDULED' AS New_Status FROM DUAL UNION ALL
    SELECT 813 AS Exam_ID, DATE '2026-09-08' AS New_Date, TIMESTAMP '2026-09-08 14:30:00' AS New_Start, TIMESTAMP '2026-09-08 16:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 814 AS Exam_ID, DATE '2026-11-16' AS New_Date, TIMESTAMP '2026-11-16 14:00:00' AS New_Start, TIMESTAMP '2026-11-16 17:00:00' AS New_End, 'SCHEDULED' AS New_Status FROM DUAL UNION ALL
    SELECT 815 AS Exam_ID, DATE '2026-09-09' AS New_Date, TIMESTAMP '2026-09-09 09:00:00' AS New_Start, TIMESTAMP '2026-09-09 10:30:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 816 AS Exam_ID, DATE '2026-10-04' AS New_Date, TIMESTAMP '2026-10-04 09:00:00' AS New_Start, TIMESTAMP '2026-10-04 12:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 817 AS Exam_ID, DATE '2026-11-17' AS New_Date, TIMESTAMP '2026-11-17 09:00:00' AS New_Start, TIMESTAMP '2026-11-17 12:00:00' AS New_End, 'SCHEDULED' AS New_Status FROM DUAL UNION ALL
    SELECT 818 AS Exam_ID, DATE '2026-09-29' AS New_Date, TIMESTAMP '2026-09-29 11:30:00' AS New_Start, TIMESTAMP '2026-09-29 13:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 819 AS Exam_ID, DATE '2026-09-09' AS New_Date, TIMESTAMP '2026-09-09 11:30:00' AS New_Start, TIMESTAMP '2026-09-09 13:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 820 AS Exam_ID, DATE '2026-09-29' AS New_Date, TIMESTAMP '2026-09-29 14:30:00' AS New_Start, TIMESTAMP '2026-09-29 16:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 821 AS Exam_ID, DATE '2026-09-09' AS New_Date, TIMESTAMP '2026-09-09 14:30:00' AS New_Start, TIMESTAMP '2026-09-09 16:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 822 AS Exam_ID, DATE '2026-11-17' AS New_Date, TIMESTAMP '2026-11-17 14:00:00' AS New_Start, TIMESTAMP '2026-11-17 17:00:00' AS New_End, 'SCHEDULED' AS New_Status FROM DUAL UNION ALL
    SELECT 823 AS Exam_ID, DATE '2026-09-30' AS New_Date, TIMESTAMP '2026-09-30 09:00:00' AS New_Start, TIMESTAMP '2026-09-30 10:30:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 824 AS Exam_ID, DATE '2026-09-30' AS New_Date, TIMESTAMP '2026-09-30 11:30:00' AS New_Start, TIMESTAMP '2026-09-30 13:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 825 AS Exam_ID, DATE '2026-09-30' AS New_Date, TIMESTAMP '2026-09-30 14:30:00' AS New_Start, TIMESTAMP '2026-09-30 16:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 826 AS Exam_ID, DATE '2026-11-18' AS New_Date, TIMESTAMP '2026-11-18 09:00:00' AS New_Start, TIMESTAMP '2026-11-18 12:00:00' AS New_End, 'SCHEDULED' AS New_Status FROM DUAL UNION ALL
    SELECT 827 AS Exam_ID, DATE '2026-10-01' AS New_Date, TIMESTAMP '2026-10-01 09:00:00' AS New_Start, TIMESTAMP '2026-10-01 10:30:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 828 AS Exam_ID, DATE '2026-09-10' AS New_Date, TIMESTAMP '2026-09-10 09:00:00' AS New_Start, TIMESTAMP '2026-09-10 10:30:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 829 AS Exam_ID, DATE '2026-09-10' AS New_Date, TIMESTAMP '2026-09-10 11:30:00' AS New_Start, TIMESTAMP '2026-09-10 13:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 830 AS Exam_ID, DATE '2026-11-18' AS New_Date, TIMESTAMP '2026-11-18 14:00:00' AS New_Start, TIMESTAMP '2026-11-18 17:00:00' AS New_End, 'SCHEDULED' AS New_Status FROM DUAL UNION ALL
    SELECT 831 AS Exam_ID, DATE '2026-10-01' AS New_Date, TIMESTAMP '2026-10-01 11:30:00' AS New_Start, TIMESTAMP '2026-10-01 13:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 832 AS Exam_ID, DATE '2026-11-19' AS New_Date, TIMESTAMP '2026-11-19 09:00:00' AS New_Start, TIMESTAMP '2026-11-19 12:00:00' AS New_End, 'SCHEDULED' AS New_Status FROM DUAL UNION ALL
    SELECT 833 AS Exam_ID, DATE '2026-09-10' AS New_Date, TIMESTAMP '2026-09-10 14:30:00' AS New_Start, TIMESTAMP '2026-09-10 16:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 834 AS Exam_ID, DATE '2026-09-11' AS New_Date, TIMESTAMP '2026-09-11 09:00:00' AS New_Start, TIMESTAMP '2026-09-11 10:30:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 835 AS Exam_ID, DATE '2026-11-19' AS New_Date, TIMESTAMP '2026-11-19 14:00:00' AS New_Start, TIMESTAMP '2026-11-19 17:00:00' AS New_End, 'SCHEDULED' AS New_Status FROM DUAL UNION ALL
    SELECT 836 AS Exam_ID, DATE '2026-09-11' AS New_Date, TIMESTAMP '2026-09-11 11:30:00' AS New_Start, TIMESTAMP '2026-09-11 13:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 837 AS Exam_ID, DATE '2026-10-01' AS New_Date, TIMESTAMP '2026-10-01 14:30:00' AS New_Start, TIMESTAMP '2026-10-01 16:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 838 AS Exam_ID, DATE '2026-10-02' AS New_Date, TIMESTAMP '2026-10-02 09:00:00' AS New_Start, TIMESTAMP '2026-10-02 10:30:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 839 AS Exam_ID, DATE '2026-09-11' AS New_Date, TIMESTAMP '2026-09-11 14:30:00' AS New_Start, TIMESTAMP '2026-09-11 16:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 840 AS Exam_ID, DATE '2026-10-02' AS New_Date, TIMESTAMP '2026-10-02 11:30:00' AS New_Start, TIMESTAMP '2026-10-02 13:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 841 AS Exam_ID, DATE '2026-10-02' AS New_Date, TIMESTAMP '2026-10-02 14:30:00' AS New_Start, TIMESTAMP '2026-10-02 16:00:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL UNION ALL
    SELECT 842 AS Exam_ID, DATE '2026-09-12' AS New_Date, TIMESTAMP '2026-09-12 09:00:00' AS New_Start, TIMESTAMP '2026-09-12 10:30:00' AS New_End, 'COMPLETED' AS New_Status FROM DUAL
) t
ON (x.Exam_ID = t.Exam_ID)
WHEN MATCHED THEN UPDATE SET
    x.Exam_Date = t.New_Date, x.Start_Time = t.New_Start, x.End_Time = t.New_End,
    x.Status = CASE WHEN t.New_Date > TRUNC(SYSDATE) THEN 'SCHEDULED' ELSE t.New_Status END
WHERE x.Exam_Date <> t.New_Date OR x.Start_Time <> t.New_Start OR x.End_Time <> t.New_End;

PROMPT [F6] Any exam dated in the future cannot be COMPLETED...
UPDATE EXAM SET Status = 'SCHEDULED'
 WHERE Status = 'COMPLETED' AND Exam_Date > TRUNC(SYSDATE)
   AND NOT EXISTS (SELECT 1 FROM EXAM_REGISTRATION er JOIN EXAM_RESULT r ON r.Exam_Registration_ID = er.Exam_Registration_ID
                    WHERE er.Exam_ID = EXAM.Exam_ID);

PROMPT [F6] Safety net: results still dated before their exam move to exam date + 7...
UPDATE EXAM_RESULT r SET Published_Date = (
        SELECT x.Exam_Date + 7 FROM EXAM_REGISTRATION er JOIN EXAM x ON x.Exam_ID = er.Exam_ID
         WHERE er.Exam_Registration_ID = r.Exam_Registration_ID)
 WHERE EXISTS (SELECT 1 FROM EXAM_REGISTRATION er JOIN EXAM x ON x.Exam_ID = er.Exam_ID
                WHERE er.Exam_Registration_ID = r.Exam_Registration_ID AND r.Published_Date < x.Exam_Date);

PROMPT [F6] Registrations created after their exam was held (no result) become CANCELLED...
UPDATE EXAM_REGISTRATION er SET Status = 'CANCELLED'
 WHERE er.Status = 'REGISTERED'
   AND er.Registration_Date > (SELECT x.Exam_Date FROM EXAM x WHERE x.Exam_ID = er.Exam_ID)
   AND NOT EXISTS (SELECT 1 FROM EXAM_RESULT r WHERE r.Exam_Registration_ID = er.Exam_Registration_ID);

PROMPT [F4] Reconciling cross-section duplicate enrollments (logical DROP, no deletes)...
-- PHASE 3 (Review 2, Appendix A Finding 4): one active section per course per term.
-- Where a student holds ENROLLED rows in two or more sections of the same Course_ID in the
-- same Semester + Academic_Year, keep the section with the most exam history (results, then
-- registrations, then the earliest Enrollment_ID) and mark the others DROPPED. No rows are
-- deleted, so ATTENDANCE, MARKS, EXAM_REGISTRATION and EXAM_RESULT history stays intact.
UPDATE ENROLLMENT SET Status = 'DROPPED'
WHERE Status = 'ENROLLED' AND Enrollment_ID IN (
    SELECT Enrollment_ID FROM (
        SELECT h.Enrollment_ID,
               ROW_NUMBER() OVER (PARTITION BY h.Student_ID, h.Course_ID, h.Semester, h.Academic_Year
                                  ORDER BY h.Result_Count DESC, h.Registration_Count DESC, h.Enrollment_ID) AS Keep_Rank
        FROM (
            SELECT en.Enrollment_ID, en.Student_ID, co.Course_ID, co.Semester, co.Academic_Year,
                   (SELECT COUNT(*) FROM EXAM_REGISTRATION er JOIN EXAM x ON x.Exam_ID = er.Exam_ID
                     JOIN EXAM_RESULT r ON r.Exam_Registration_ID = er.Exam_Registration_ID
                     WHERE er.Student_ID = en.Student_ID AND x.Offering_ID = en.Offering_ID) AS Result_Count,
                   (SELECT COUNT(*) FROM EXAM_REGISTRATION er JOIN EXAM x ON x.Exam_ID = er.Exam_ID
                     WHERE er.Student_ID = en.Student_ID AND x.Offering_ID = en.Offering_ID) AS Registration_Count
            FROM ENROLLMENT en JOIN COURSE_OFFERING co ON co.Offering_ID = en.Offering_ID
            WHERE en.Status = 'ENROLLED'
        ) h
    ) WHERE Keep_Rank > 1
);

COMMIT;

PROMPT [F8][F2] Adding named CHECK constraints (5 status columns + 3 marks ceilings)...
DECLARE
    PROCEDURE add_check(p_table VARCHAR2, p_name VARCHAR2, p_condition VARCHAR2) IS
        v_count NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_count FROM USER_CONSTRAINTS WHERE Constraint_Name = p_name;
        IF v_count = 0 THEN
            EXECUTE IMMEDIATE 'ALTER TABLE ' || p_table || ' ADD CONSTRAINT ' || p_name || ' CHECK (' || p_condition || ')';
            DBMS_OUTPUT.PUT_LINE('  Added ' || p_name);
        ELSE
            DBMS_OUTPUT.PUT_LINE('  ' || p_name || ' already present');
        END IF;
    END;
BEGIN
    add_check('ENROLLMENT', 'CK_ENROLLMENT_STATUS', 'Status IN (''ENROLLED'', ''DROPPED'', ''COMPLETED'')');
    add_check('EXAM', 'CK_EXAM_STATUS', 'Status IN (''SCHEDULED'', ''COMPLETED'', ''CANCELLED'')');
    add_check('EXAM_REGISTRATION', 'CK_EXAMREG_STATUS', 'Status IN (''REGISTERED'', ''CANCELLED'')');
    add_check('EXAM_RESULT', 'CK_RESULT_STATUS', 'Result_Status IN (''PASS'', ''FAIL'', ''WITHHELD'')');
    add_check('RE_EXAM_APPLICATION', 'CK_REEXAM_STATUS', 'Status IN (''PENDING'', ''APPROVED'', ''REJECTED'')');
    add_check('MARKS', 'CK_MARKS_CA', 'Continuous_Assessment_Marks BETWEEN 0 AND 30');
    add_check('MARKS', 'CK_MARKS_MIDTERM', 'Midterm_Marks BETWEEN 0 AND 30');
    add_check('MARKS', 'CK_MARKS_FINAL', 'Final_Exam_Marks BETWEEN 0 AND 50');
END;
/

PROMPT Migration complete. Run 03_verify_post_migration.sql next.
