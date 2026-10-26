-- ==============================================================================
-- DEVELOPMENT UTILITY SCRIPT: 04_provision_dev_students.sql
-- Smart Campus Management System — XYZ University
--
-- PURPOSE:
--   Controlled development-only script that provisions credentials for existing
--   records in the STUDENT table using Student_ID as Username and BCrypt hash of '1234'.
--
-- SECURITY ADVISORY:
--   - Passwords are salted and hashed using BCrypt ($2a$10$...).
--   - '1234' is a temporary development credential for demonstration purposes ONLY.
--   - In real-world deployment, students must be issued individual secure passwords.
--   - By default, this script only inserts accounts for unprovisioned students.
-- ==============================================================================

SET SERVEROUTPUT ON SIZE UNLIMITED;
SET LINESIZE 200;

DECLARE
    v_hash VARCHAR2(100) := '$2a$10$DzCZdFpiaTugCwgY5bK9LO..VM3enoPmmJnwNOJhstHOkePbmzL1i'; -- BCrypt('1234')
    v_count NUMBER := 0;
    v_existing NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('-----------------------------------------------------------------');
    DBMS_OUTPUT.PUT_LINE('PROVISIONING UNPROVISIONED STUDENTS WITH TEMP CREDENTIALS...');
    DBMS_OUTPUT.PUT_LINE('-----------------------------------------------------------------');

    FOR s IN (SELECT Student_ID, Name, Email FROM STUDENT ORDER BY Student_ID ASC) LOOP
        SELECT COUNT(*) INTO v_existing FROM USER_ACCOUNT WHERE Student_ID = s.Student_ID;
        
        IF v_existing = 0 THEN
            INSERT INTO USER_ACCOUNT (Username, Password_Hash, Role, Student_ID, Is_Active, Created_At)
            VALUES (TO_CHAR(s.Student_ID), v_hash, 'STUDENT', s.Student_ID, 1, CURRENT_TIMESTAMP);
            v_count := v_count + 1;
            DBMS_OUTPUT.PUT_LINE('[PROVISIONED] Student #' || s.Student_ID || ' (' || s.Name || ') -> Username: ' || s.Student_ID || ' / Pass: 1234');
        ELSE
            DBMS_OUTPUT.PUT_LINE('[SKIPPED]     Student #' || s.Student_ID || ' (' || s.Name || ') already has credentials.');
        END IF;
    END LOOP;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('-----------------------------------------------------------------');
    DBMS_OUTPUT.PUT_LINE('Total students newly provisioned: ' || v_count);
    DBMS_OUTPUT.PUT_LINE('-----------------------------------------------------------------');
END;
/
