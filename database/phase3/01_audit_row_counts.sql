-- ==============================================================================
-- PHASE 3 AUDIT: 01_audit_row_counts.sql  (Review 2, Appendix A Finding 1)
-- Read-only. Compares live row counts in all 15 tables with the seeded baseline.
--
-- Seeded baseline = 1,712 rows:
--   * 1,636 academic rows inserted by sql/02_insert_sample_data.sql (45 rows)
--     + sql/03_expand_sample_data.sql (1,591 rows)
--   *    76 login rows created by sql/04_authentication_migration.sql
--        (1 administrator + 60 students + 15 faculty)
-- The figure 1,896 quoted in Review 2 was the LINE count of dataset_export.txt
-- (headers, separators and blank lines included), not a row count.
-- Any surplus over the baseline is data created through the portal after seeding.
-- ==============================================================================
SET PAGESIZE 100 LINESIZE 160 FEEDBACK OFF
COLUMN table_name FORMAT A22
PROMPT
PROMPT === Finding 1: live row counts vs seeded baseline (1,712) ===
WITH live AS (
    SELECT 'DEPARTMENT' AS table_name, 1 AS ord, 6 AS seeded, (SELECT COUNT(*) FROM DEPARTMENT) AS live_rows FROM DUAL
    UNION ALL SELECT 'PROGRAM', 2, 10, (SELECT COUNT(*) FROM PROGRAM) FROM DUAL
    UNION ALL SELECT 'STUDENT', 3, 60, (SELECT COUNT(*) FROM STUDENT) FROM DUAL
    UNION ALL SELECT 'FACULTY', 4, 15, (SELECT COUNT(*) FROM FACULTY) FROM DUAL
    UNION ALL SELECT 'COURSE', 5, 30, (SELECT COUNT(*) FROM COURSE) FROM DUAL
    UNION ALL SELECT 'COURSE_OFFERING', 6, 45, (SELECT COUNT(*) FROM COURSE_OFFERING) FROM DUAL
    UNION ALL SELECT 'ENROLLMENT', 7, 365, (SELECT COUNT(*) FROM ENROLLMENT) FROM DUAL
    UNION ALL SELECT 'ATTENDANCE', 8, 365, (SELECT COUNT(*) FROM ATTENDANCE) FROM DUAL
    UNION ALL SELECT 'MARKS', 9, 250, (SELECT COUNT(*) FROM MARKS) FROM DUAL
    UNION ALL SELECT 'GRADE', 10, 6, (SELECT COUNT(*) FROM GRADE) FROM DUAL
    UNION ALL SELECT 'EXAM', 11, 42, (SELECT COUNT(*) FROM EXAM) FROM DUAL
    UNION ALL SELECT 'EXAM_REGISTRATION', 12, 232, (SELECT COUNT(*) FROM EXAM_REGISTRATION) FROM DUAL
    UNION ALL SELECT 'EXAM_RESULT', 13, 190, (SELECT COUNT(*) FROM EXAM_RESULT) FROM DUAL
    UNION ALL SELECT 'RE_EXAM_APPLICATION', 14, 20, (SELECT COUNT(*) FROM RE_EXAM_APPLICATION) FROM DUAL
    UNION ALL SELECT 'USER_ACCOUNT', 15, 76, (SELECT COUNT(*) FROM USER_ACCOUNT) FROM DUAL
)
SELECT table_name, seeded, live_rows, live_rows - seeded AS added_via_portal
FROM (
    SELECT table_name, ord, seeded, live_rows FROM live
    UNION ALL
    SELECT 'TOTAL', 99, SUM(seeded), SUM(live_rows) FROM live
)
ORDER BY ord;
