-- ==============================================================================
-- PHASE 3 VERIFY: 03_verify_post_migration.sql
-- Proves that the new constraints are enforced by Oracle itself. Every probe runs
-- inside a PL/SQL block that ROLLS BACK, so no test row is ever left behind.
-- Prints PASS/FAIL per probe and a final tally.
-- Run 01_audit_all.sql afterwards to confirm every violation count is 0.
-- ==============================================================================
SET SERVEROUTPUT ON SIZE UNLIMITED
SET FEEDBACK OFF
SET DEFINE OFF

DECLARE
    v_pass PLS_INTEGER := 0;
    v_fail PLS_INTEGER := 0;
    v_course NUMBER;
    v_faculty NUMBER;
    v_student NUMBER;
    v_offering NUMBER;

    PROCEDURE expect(p_label VARCHAR2, p_sql VARCHAR2, p_should_succeed BOOLEAN) IS
        v_ok BOOLEAN;
        v_err VARCHAR2(200);
    BEGIN
        SAVEPOINT probe;
        BEGIN
            EXECUTE IMMEDIATE p_sql;
            v_ok := TRUE;
        EXCEPTION WHEN OTHERS THEN
            v_ok := FALSE;
            v_err := SUBSTR(SQLERRM, 1, 120);
        END;
        ROLLBACK TO probe;
        IF v_ok = p_should_succeed THEN
            v_pass := v_pass + 1;
            DBMS_OUTPUT.PUT_LINE('PASS  ' || p_label || CASE WHEN NOT v_ok THEN '  -> ' || v_err END);
        ELSE
            v_fail := v_fail + 1;
            DBMS_OUTPUT.PUT_LINE('FAIL  ' || p_label || CASE WHEN NOT v_ok THEN '  -> ' || v_err END);
        END IF;
    END;
BEGIN
    SELECT MIN(Course_ID) INTO v_course FROM COURSE;
    SELECT MIN(Faculty_ID) INTO v_faculty FROM FACULTY;
    SELECT MIN(Student_ID) INTO v_student FROM STUDENT;
    SELECT MIN(Offering_ID) INTO v_offering FROM COURSE_OFFERING;

    DBMS_OUTPUT.PUT_LINE('--- F7: Semester range 1..10 on COURSE_OFFERING ---');
    FOR s IN (SELECT 1 AS sem, 1 AS ok FROM DUAL UNION ALL SELECT 10, 1 FROM DUAL
              UNION ALL SELECT 0, 0 FROM DUAL UNION ALL SELECT 11, 0 FROM DUAL) LOOP
        expect('semester ' || s.sem || CASE s.ok WHEN 1 THEN ' accepted' ELSE ' rejected' END,
               'INSERT INTO COURSE_OFFERING (Offering_ID, Course_ID, Faculty_ID, Semester, Academic_Year, Section, Room_No, Max_Seats, Classes_Held) '
               || 'VALUES (999990, ' || v_course || ', ' || v_faculty || ', ' || s.sem || ', ''2099-2100'', ''ZZ'', ''TEST'', 10, 0)',
               s.ok = 1);
    END LOOP;

    DBMS_OUTPUT.PUT_LINE('--- F8: status CHECK constraints ---');
    expect('ENROLLMENT.Status = DROPPED accepted',
           'UPDATE ENROLLMENT SET Status = ''DROPPED'' WHERE ROWNUM = 1', TRUE);
    expect('ENROLLMENT.Status = BOGUS rejected',
           'UPDATE ENROLLMENT SET Status = ''BOGUS'' WHERE ROWNUM = 1', FALSE);
    expect('EXAM.Status = CANCELLED accepted',
           'UPDATE EXAM SET Status = ''CANCELLED'' WHERE ROWNUM = 1', TRUE);
    expect('EXAM.Status = DONE rejected',
           'UPDATE EXAM SET Status = ''DONE'' WHERE ROWNUM = 1', FALSE);
    expect('EXAM_REGISTRATION.Status = CANCELLED accepted',
           'UPDATE EXAM_REGISTRATION SET Status = ''CANCELLED'' WHERE ROWNUM = 1', TRUE);
    expect('EXAM_REGISTRATION.Status = MAYBE rejected',
           'UPDATE EXAM_REGISTRATION SET Status = ''MAYBE'' WHERE ROWNUM = 1', FALSE);
    expect('EXAM_RESULT.Result_Status = WITHHELD accepted',
           'UPDATE EXAM_RESULT SET Result_Status = ''WITHHELD'' WHERE ROWNUM = 1', TRUE);
    expect('EXAM_RESULT.Result_Status = PUBLISHED rejected',
           'UPDATE EXAM_RESULT SET Result_Status = ''PUBLISHED'' WHERE ROWNUM = 1', FALSE);
    expect('RE_EXAM_APPLICATION.Status = APPROVED accepted',
           'UPDATE RE_EXAM_APPLICATION SET Status = ''APPROVED'' WHERE ROWNUM = 1', TRUE);
    expect('RE_EXAM_APPLICATION.Status = ON_HOLD rejected',
           'UPDATE RE_EXAM_APPLICATION SET Status = ''ON_HOLD'' WHERE ROWNUM = 1', FALSE);

    DBMS_OUTPUT.PUT_LINE('--- F2: MARKS component ceilings (30 / 30 / 50, total 110) ---');
    expect('marks 30/30/50 (total 110) accepted',
           'UPDATE MARKS SET Continuous_Assessment_Marks = 30, Midterm_Marks = 30, Final_Exam_Marks = 50 WHERE ROWNUM = 1', TRUE);
    expect('CA 30.5 rejected', 'UPDATE MARKS SET Continuous_Assessment_Marks = 30.5 WHERE ROWNUM = 1', FALSE);
    expect('Midterm 31 rejected', 'UPDATE MARKS SET Midterm_Marks = 31 WHERE ROWNUM = 1', FALSE);
    expect('Final 51 rejected', 'UPDATE MARKS SET Final_Exam_Marks = 51 WHERE ROWNUM = 1', FALSE);
    expect('negative CA rejected', 'UPDATE MARKS SET Continuous_Assessment_Marks = -1 WHERE ROWNUM = 1', FALSE);

    DBMS_OUTPUT.PUT_LINE('--- Existing integrity rules still enforced ---');
    expect('duplicate (Student, Offering) enrollment rejected by UQ_ENROLLMENT',
           'INSERT INTO ENROLLMENT (Enrollment_ID, Student_ID, Offering_ID, Status) '
           || 'SELECT 999990, Student_ID, Offering_ID, ''ENROLLED'' FROM ENROLLMENT WHERE ROWNUM = 1', FALSE);
    expect('exam with End_Time <= Start_Time rejected by CK_EXAM_TIME',
           'INSERT INTO EXAM (Exam_ID, Offering_ID, Exam_Type, Exam_Date, Start_Time, End_Time, Venue, Max_Marks) '
           || 'VALUES (999990, ' || v_offering || ', ''CAT1'', DATE ''2099-01-01'', TIMESTAMP ''2099-01-01 10:00:00'', TIMESTAMP ''2099-01-01 09:00:00'', ''X'', 50)', FALSE);
    expect('enrollment for missing student rejected by FK_ENROLLMENT_STUDENT',
           'INSERT INTO ENROLLMENT (Enrollment_ID, Student_ID, Offering_ID) VALUES (999990, -1, ' || v_offering || ')', FALSE);
    expect('deleting a STUDENT with academic history rejected (child FK rows)',
           'DELETE FROM STUDENT WHERE Student_ID = ' || v_student, FALSE);

    ROLLBACK;
    DBMS_OUTPUT.PUT_LINE('---');
    DBMS_OUTPUT.PUT_LINE('CONSTRAINT PROBES: ' || v_pass || ' passed, ' || v_fail || ' failed');
END;
/
