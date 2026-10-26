-- ==============================================================================
-- DEVELOPMENT UTILITY SCRIPT: 04_reset_student_passwords_dev.sql
-- Smart Campus Management System — XYZ University
--
-- PURPOSE:
--   Explicit reset operation that updates existing student accounts to the
--   temporary development password '1234' (BCrypt hashed).
--
-- CAUTION:
--   - This operation will overwrite existing password hashes for students.
--   - Requires explicit administrative authorization.
-- ==============================================================================

SET SERVEROUTPUT ON SIZE UNLIMITED;
SET LINESIZE 200;

DECLARE
    v_hash VARCHAR2(100) := '$2a$10$DzCZdFpiaTugCwgY5bK9LO..VM3enoPmmJnwNOJhstHOkePbmzL1i'; -- BCrypt('1234')
    v_count NUMBER := 0;
BEGIN
    DBMS_OUTPUT.PUT_LINE('-----------------------------------------------------------------');
    DBMS_OUTPUT.PUT_LINE('RESETTING STUDENT PASSWORDS TO TEMPORARY DEVELOPMENT PASSWORD...');
    DBMS_OUTPUT.PUT_LINE('-----------------------------------------------------------------');

    FOR s IN (SELECT u.Account_ID, u.Student_ID, st.Name 
              FROM USER_ACCOUNT u 
              JOIN STUDENT st ON u.Student_ID = st.Student_ID 
              WHERE u.Role = 'STUDENT') LOOP
        UPDATE USER_ACCOUNT
        SET Password_Hash = v_hash,
            Is_Active = 1
        WHERE Account_ID = s.Account_ID;
        
        v_count := v_count + 1;
        DBMS_OUTPUT.PUT_LINE('[RESET] Student #' || s.Student_ID || ' (' || s.Name || ') password reset to 1234');
    END LOOP;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('-----------------------------------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Total student accounts reset: ' || v_count);
    DBMS_OUTPUT.PUT_LINE('-----------------------------------------------------------------');
END;
/
