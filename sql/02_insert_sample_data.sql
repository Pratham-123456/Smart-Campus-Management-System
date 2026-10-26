
SET DEFINE OFF

-- 1. DEPARTMENT
INSERT INTO DEPARTMENT (Department_ID, Department_Name, Office_Location) VALUES (1, 'Computer Science', 'SJT');
INSERT INTO DEPARTMENT (Department_ID, Department_Name, Office_Location) VALUES (2, 'Electronics', 'TT');

-- 2. PROGRAM
INSERT INTO PROGRAM (Program_ID, Program_Name, Degree, Duration_Years, Department_ID) VALUES (1, 'Computer Science Engineering', 'B.Tech', 4, 1);
INSERT INTO PROGRAM (Program_ID, Program_Name, Degree, Duration_Years, Department_ID) VALUES (2, 'Electronics Engineering', 'B.Tech', 4, 2);

-- 3. STUDENT
INSERT INTO STUDENT (Student_ID, Name, Email, Phone, DOB, Admission_Year, Program_ID) VALUES (1001, 'Aarav Sharma', 'aarav.sharma@example.com', '9876500001', DATE '2006-03-15', 2025, 1);
INSERT INTO STUDENT (Student_ID, Name, Email, Phone, DOB, Admission_Year, Program_ID) VALUES (1002, 'Ananya Verma', 'ananya.verma@example.com', '9876500002', DATE '2006-07-22', 2025, 1);
INSERT INTO STUDENT (Student_ID, Name, Email, Phone, DOB, Admission_Year, Program_ID) VALUES (1003, 'Rohan Mehta', 'rohan.mehta@example.com', '9876500003', DATE '2005-11-08', 2025, 2);

-- 4. FACULTY
INSERT INTO FACULTY (Faculty_ID, Name, Email, Phone, Designation, Department_ID) VALUES (201, 'Dr. Meera Rao', 'meera.rao@example.com', '9876510001', 'Professor', 1);
INSERT INTO FACULTY (Faculty_ID, Name, Email, Phone, Designation, Department_ID) VALUES (202, 'Dr. Karan Singh', 'karan.singh@example.com', '9876510002', 'Associate Professor', 2);

-- 5. COURSE
INSERT INTO COURSE (Course_ID, Course_Name, Credits, Course_Type, Department_ID) VALUES (301, 'Database Systems', 4, 'Core', 1);
INSERT INTO COURSE (Course_ID, Course_Name, Credits, Course_Type, Department_ID) VALUES (302, 'Computer Networks', 3, 'Core', 1);
INSERT INTO COURSE (Course_ID, Course_Name, Credits, Course_Type, Department_ID) VALUES (303, 'Digital Electronics', 3, 'Core', 2);

-- 6. COURSE_OFFERING
INSERT INTO COURSE_OFFERING (Offering_ID, Course_ID, Faculty_ID, Semester, Academic_Year, Section, Room_No, Max_Seats, Classes_Held) VALUES (401, 301, 201, 3, '2026-2027', 'A', 'SJT-301', 60, 30);
INSERT INTO COURSE_OFFERING (Offering_ID, Course_ID, Faculty_ID, Semester, Academic_Year, Section, Room_No, Max_Seats, Classes_Held) VALUES (402, 302, 201, 3, '2026-2027', 'A', 'SJT-302', 60, 28);
INSERT INTO COURSE_OFFERING (Offering_ID, Course_ID, Faculty_ID, Semester, Academic_Year, Section, Room_No, Max_Seats, Classes_Held) VALUES (403, 303, 202, 3, '2026-2027', 'B', 'TT-201', 50, 25);

-- 7. ENROLLMENT
INSERT INTO ENROLLMENT (Enrollment_ID, Student_ID, Offering_ID, Enrollment_Date, Status) VALUES (501, 1001, 401, DATE '2026-07-15', 'ENROLLED');
INSERT INTO ENROLLMENT (Enrollment_ID, Student_ID, Offering_ID, Enrollment_Date, Status) VALUES (502, 1002, 401, DATE '2026-07-15', 'ENROLLED');
INSERT INTO ENROLLMENT (Enrollment_ID, Student_ID, Offering_ID, Enrollment_Date, Status) VALUES (503, 1001, 402, DATE '2026-07-15', 'ENROLLED');
INSERT INTO ENROLLMENT (Enrollment_ID, Student_ID, Offering_ID, Enrollment_Date, Status) VALUES (504, 1003, 403, DATE '2026-07-15', 'ENROLLED');

-- 8. ATTENDANCE
INSERT INTO ATTENDANCE (Attendance_ID, Student_ID, Offering_ID, Classes_Attended) VALUES (601, 1001, 401, 27);
INSERT INTO ATTENDANCE (Attendance_ID, Student_ID, Offering_ID, Classes_Attended) VALUES (602, 1002, 401, 24);
INSERT INTO ATTENDANCE (Attendance_ID, Student_ID, Offering_ID, Classes_Attended) VALUES (603, 1001, 402, 25);
INSERT INTO ATTENDANCE (Attendance_ID, Student_ID, Offering_ID, Classes_Attended) VALUES (604, 1003, 403, 22);

-- 9. MARKS
INSERT INTO MARKS (Mark_ID, Student_ID, Offering_ID, Continuous_Assessment_Marks, Midterm_Marks, Final_Exam_Marks) VALUES (701, 1001, 401, 18, 24, 42);
INSERT INTO MARKS (Mark_ID, Student_ID, Offering_ID, Continuous_Assessment_Marks, Midterm_Marks, Final_Exam_Marks) VALUES (702, 1002, 401, 16, 21, 38);
INSERT INTO MARKS (Mark_ID, Student_ID, Offering_ID, Continuous_Assessment_Marks, Midterm_Marks, Final_Exam_Marks) VALUES (703, 1001, 402, 17, 22, 40);
INSERT INTO MARKS (Mark_ID, Student_ID, Offering_ID, Continuous_Assessment_Marks, Midterm_Marks, Final_Exam_Marks) VALUES (704, 1003, 403, 19, 23, 41);

-- 10. GRADE
INSERT INTO GRADE (Grade_Code, Grade_Point, Min_Percentage, Max_Percentage) VALUES ('S', 10, 90, 100);
INSERT INTO GRADE (Grade_Code, Grade_Point, Min_Percentage, Max_Percentage) VALUES ('A', 9, 80, 89.99);
INSERT INTO GRADE (Grade_Code, Grade_Point, Min_Percentage, Max_Percentage) VALUES ('B', 8, 70, 79.99);
INSERT INTO GRADE (Grade_Code, Grade_Point, Min_Percentage, Max_Percentage) VALUES ('C', 7, 60, 69.99);
INSERT INTO GRADE (Grade_Code, Grade_Point, Min_Percentage, Max_Percentage) VALUES ('D', 6, 50, 59.99);
INSERT INTO GRADE (Grade_Code, Grade_Point, Min_Percentage, Max_Percentage) VALUES ('F', 0, 0, 49.99);

-- 11. EXAM
INSERT INTO EXAM (Exam_ID, Offering_ID, Exam_Type, Exam_Date, Start_Time, End_Time, Venue, Max_Marks, Status) VALUES (801, 401, 'CAT1', DATE '2026-09-10', TIMESTAMP '2026-09-10 09:00:00', TIMESTAMP '2026-09-10 10:00:00', 'SJT-301', 50, 'COMPLETED');
INSERT INTO EXAM (Exam_ID, Offering_ID, Exam_Type, Exam_Date, Start_Time, End_Time, Venue, Max_Marks, Status) VALUES (802, 402, 'CAT1', DATE '2026-09-12', TIMESTAMP '2026-09-12 11:00:00', TIMESTAMP '2026-09-12 12:00:00', 'SJT-302', 50, 'COMPLETED');
INSERT INTO EXAM (Exam_ID, Offering_ID, Exam_Type, Exam_Date, Start_Time, End_Time, Venue, Max_Marks, Status) VALUES (803, 403, 'CAT1', DATE '2026-09-15', TIMESTAMP '2026-09-15 14:00:00', TIMESTAMP '2026-09-15 15:00:00', 'TT-201', 50, 'COMPLETED');

-- 12. EXAM_REGISTRATION
INSERT INTO EXAM_REGISTRATION (Exam_Registration_ID, Student_ID, Exam_ID, Registration_Date, Status) VALUES (901, 1001, 801, DATE '2026-08-20', 'REGISTERED');
INSERT INTO EXAM_REGISTRATION (Exam_Registration_ID, Student_ID, Exam_ID, Registration_Date, Status) VALUES (902, 1002, 801, DATE '2026-08-20', 'REGISTERED');
INSERT INTO EXAM_REGISTRATION (Exam_Registration_ID, Student_ID, Exam_ID, Registration_Date, Status) VALUES (903, 1001, 802, DATE '2026-08-20', 'REGISTERED');
INSERT INTO EXAM_REGISTRATION (Exam_Registration_ID, Student_ID, Exam_ID, Registration_Date, Status) VALUES (904, 1003, 803, DATE '2026-08-20', 'REGISTERED');

-- 13. EXAM_RESULT
INSERT INTO EXAM_RESULT (Exam_Result_ID, Exam_Registration_ID, Marks_Obtained, Result_Status, Grade_Code, Published_Date) VALUES (10001, 901, 46, 'PASS', 'S', DATE '2026-09-20');
INSERT INTO EXAM_RESULT (Exam_Result_ID, Exam_Registration_ID, Marks_Obtained, Result_Status, Grade_Code, Published_Date) VALUES (10002, 902, 38, 'PASS', 'A', DATE '2026-09-20');
INSERT INTO EXAM_RESULT (Exam_Result_ID, Exam_Registration_ID, Marks_Obtained, Result_Status, Grade_Code, Published_Date) VALUES (10003, 903, 42, 'PASS', 'S', DATE '2026-09-22');
INSERT INTO EXAM_RESULT (Exam_Result_ID, Exam_Registration_ID, Marks_Obtained, Result_Status, Grade_Code, Published_Date) VALUES (10004, 904, 28, 'PASS', 'B', DATE '2026-09-22');

-- 14. RE_EXAM_APPLICATION
INSERT INTO RE_EXAM_APPLICATION (ReExam_Application_ID, Exam_Result_ID, Application_Date, Reason, Status, Approval_Date) VALUES (11001, 10002, DATE '2026-09-25', 'Request for re-examination due to exceptional circumstances', 'PENDING', NULL);

COMMIT;

-- Verification
SELECT 'DEPARTMENT' AS table_name, COUNT(*) AS row_count FROM DEPARTMENT UNION ALL SELECT 'PROGRAM', COUNT(*) FROM PROGRAM UNION ALL SELECT 'STUDENT', COUNT(*) FROM STUDENT UNION ALL SELECT 'FACULTY', COUNT(*) FROM FACULTY UNION ALL SELECT 'COURSE', COUNT(*) FROM COURSE UNION ALL SELECT 'COURSE_OFFERING', COUNT(*) FROM COURSE_OFFERING UNION ALL SELECT 'ENROLLMENT', COUNT(*) FROM ENROLLMENT UNION ALL SELECT 'ATTENDANCE', COUNT(*) FROM ATTENDANCE UNION ALL SELECT 'MARKS', COUNT(*) FROM MARKS UNION ALL SELECT 'GRADE', COUNT(*) FROM GRADE UNION ALL SELECT 'EXAM', COUNT(*) FROM EXAM UNION ALL SELECT 'EXAM_REGISTRATION', COUNT(*) FROM EXAM_REGISTRATION UNION ALL SELECT 'EXAM_RESULT', COUNT(*) FROM EXAM_RESULT UNION ALL SELECT 'RE_EXAM_APPLICATION', COUNT(*) FROM RE_EXAM_APPLICATION;
SELECT 'DML FILE WORKS' AS message FROM DUAL;