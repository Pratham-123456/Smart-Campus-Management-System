# =============================================================================
# End-to-End Workflow Verification Suite - XYZ University Smart Campus
# Runs against the LIVE application (http://localhost:8080) and Oracle (oracle-db).
#
#   Part A: the original 15 Review 2 scenarios (kept, made repeatable)
#   Part B: Phase 3 scenarios for the Review 2 Appendix A fixes and CRUD lifecycle
#
# Part B builds its own fixtures through the admin REST API (department, program,
# courses, faculty, students, offerings, exams, accounts) and removes them at the end,
# so the seeded data is left as it was. Part A scenarios that write to seeded rows
# restore the original values afterwards.
#
# Usage:  powershell -ExecutionPolicy Bypass -File scripts\verify_workflows.ps1
# Needs:  app on :8080, docker container oracle-db, dev logins (password 1234).
# =============================================================================

$ErrorActionPreference = "Continue"
$baseUrl = "http://localhost:8080"
$script:passCount = 0
$script:failCount = 0
$script:results = @()

# --- DB password: CAMPUS_DB_PASSWORD (process, then user scope), else a hidden prompt (never printed)
$dbPassword = $env:CAMPUS_DB_PASSWORD
if ([string]::IsNullOrWhiteSpace($dbPassword)) { $dbPassword = [Environment]::GetEnvironmentVariable("CAMPUS_DB_PASSWORD", "User") }
if ([string]::IsNullOrWhiteSpace($dbPassword)) {
    $secure = Read-Host "Enter the Oracle password for campus_app" -AsSecureString
    $dbPassword = [System.Net.NetworkCredential]::new("", $secure).Password
}

function Record-Test {
    param([string]$Id, [string]$Description, [bool]$Success, [string]$Details)
    if ($Success) {
        $script:passCount++
        Write-Host " [PASS] $($Id): $Description" -ForegroundColor Green
        if ($Details) { Write-Host "        $Details" -ForegroundColor DarkGreen }
    } else {
        $script:failCount++
        Write-Host " [FAIL] $($Id): $Description" -ForegroundColor Red
        if ($Details) { Write-Host "        $Details" -ForegroundColor DarkRed }
    }
    $script:results += [pscustomobject]@{ Id = $Id; Pass = $Success }
}

# Calls the API and never throws: returns @{ Status = <int>; Json = <object or $null> }
function Invoke-Api {
    param([string]$Method, [string]$Path, $Body = $null, $Session = $null)
    $params = @{ Uri = "$baseUrl$Path"; Method = $Method; UseBasicParsing = $true; ContentType = "application/json" }
    if ($Session) { $params.WebSession = $Session }
    if ($null -ne $Body) { $params.Body = ($Body | ConvertTo-Json -Depth 5) }
    try {
        $resp = Invoke-WebRequest @params
        $json = $null
        if ($resp.Content) { $json = $resp.Content | ConvertFrom-Json }
        return @{ Status = [int]$resp.StatusCode; Json = $json }
    } catch {
        $status = 0; $json = $null
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
            try {
                $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
                $text = $reader.ReadToEnd()
                if ($text) { $json = $text | ConvertFrom-Json }
            } catch { }
        }
        return @{ Status = $status; Json = $json }
    }
}

function New-Session { return New-Object Microsoft.PowerShell.Commands.WebRequestSession }

function Login {
    param([string]$Username, [string]$Password, [string]$Role)
    $s = New-Session
    $r = Invoke-Api POST "/api/auth/login" @{ username = $Username; password = $Password; role = $Role } $s
    if ($r.Status -ne 200) { throw "Login failed for $Role $Username (HTTP $($r.Status)): $($r.Json.message)" }
    return @{ Session = $s; User = $r.Json.data }
}

# Runs SQL through SQL*Plus inside the container; the connect string goes via stdin.
function Invoke-Sql {
    param([string]$Sql)
    # SQL*Plus executes only one statement per line, so put each ';'-terminated statement on its own line
    $Sql = $Sql -replace ';[ \t]+(?=\S)', ";`n"
    $script = "CONNECT campus_app/`"$dbPassword`"@//localhost:1521/FREEPDB1`nSET PAGESIZE 0 FEEDBACK OFF HEADING OFF VERIFY OFF`n$Sql`nEXIT`n"
    return ($script | docker exec -i oracle-db sqlplus -s /nolog) -join "`n"
}

function Expect {
    param([string]$Id, [string]$Description, $Response, [int]$Expected, [string]$MessageLike = $null)
    $ok = ($Response.Status -eq $Expected)
    if ($ok -and $MessageLike) { $ok = ($Response.Json.message -match $MessageLike) }
    Record-Test $Id $Description $ok "HTTP $($Response.Status) (expected $Expected): $($Response.Json.message)"
}

Write-Host "=========================================================================" -ForegroundColor Cyan
Write-Host " XYZ UNIVERSITY SMART CAMPUS - END-TO-END WORKFLOW VERIFICATION" -ForegroundColor Cyan
Write-Host " Target: $baseUrl   Run at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "=========================================================================" -ForegroundColor Cyan

$health = Invoke-Api GET "/api/auth/health"
if ($health.Status -ne 200) {
    Write-Host " Application is not reachable at $baseUrl (HTTP $($health.Status)). Start it with run.bat first." -ForegroundColor Red
    exit 2
}

Write-Host ""
Write-Host "--- PART A: Review 2 scenarios 1-15 ---" -ForegroundColor Cyan

# A1: Student logs in and views own enrollments
try {
    $stu = Login "1001" "1234" "STUDENT"; $studentSession = $stu.Session
    $r = Invoke-Api GET "/api/student/1001/enrollments" $null $studentSession
    Record-Test "A1" "Student log in and view own enrollments" ($r.Status -eq 200 -and $r.Json.data.Count -gt 0) "Student 1001 ($($stu.User.name)) sees $($r.Json.data.Count) enrollment rows."
} catch { Record-Test "A1" "Student log in and view own enrollments" $false "$_" }

# A2: Duplicate registration in the same offering -> 409
$r = Invoke-Api POST "/api/student/1001/enroll" @{ offeringId = 401 } $studentSession
Expect "A2" "Duplicate course registration rejected" $r 409 "already enrolled"

# A3: Full offering -> 409 (Max_Seats temporarily set to the enrolled count, then restored; any row the test creates is removed)
try {
    # Offering with the most active enrollments (so "full" really means full)
    $capOff = (Invoke-Sql "SELECT Offering_ID FROM (SELECT Offering_ID FROM ENROLLMENT WHERE Status = 'ENROLLED' GROUP BY Offering_ID ORDER BY COUNT(*) DESC, Offering_ID) WHERE ROWNUM = 1;").Trim()
    $origSeats = (Invoke-Sql "SELECT Max_Seats FROM COURSE_OFFERING WHERE Offering_ID = $capOff;").Trim()
    Invoke-Sql "UPDATE COURSE_OFFERING SET Max_Seats = GREATEST(1, (SELECT COUNT(*) FROM ENROLLMENT WHERE Offering_ID = $capOff AND Status = 'ENROLLED')) WHERE Offering_ID = $capOff; COMMIT;" | Out-Null
    # A seeded student with a login who holds no section of offering $capOff's course this term
    $capStudent = (Invoke-Sql "SELECT MIN(s.Student_ID) FROM STUDENT s JOIN USER_ACCOUNT ua ON ua.Student_ID = s.Student_ID AND ua.Is_Active = 1 WHERE s.Student_ID BETWEEN 1001 AND 1060 AND NOT EXISTS (SELECT 1 FROM ENROLLMENT e JOIN COURSE_OFFERING o ON o.Offering_ID = e.Offering_ID JOIN COURSE_OFFERING t ON t.Offering_ID = $capOff WHERE e.Student_ID = s.Student_ID AND e.Status = 'ENROLLED' AND o.Course_ID = t.Course_ID AND o.Semester = t.Semester AND o.Academic_Year = t.Academic_Year);").Trim()
    $sCap = Login $capStudent "1234" "STUDENT"
    $r = Invoke-Api POST "/api/student/$capStudent/enroll" @{ offeringId = $capOff } $sCap.Session
    if ($r.Status -eq 201) {
        Invoke-Sql "DELETE FROM ATTENDANCE WHERE Student_ID = $capStudent AND Offering_ID = $capOff; DELETE FROM MARKS WHERE Student_ID = $capStudent AND Offering_ID = $capOff; DELETE FROM ENROLLMENT WHERE Student_ID = $capStudent AND Offering_ID = $capOff; COMMIT;" | Out-Null
    }
    Expect "A3" "Full course capacity rejected (student $capStudent -> offering $capOff)" $r 409 "full capacity"
} catch { Record-Test "A3" "Full course capacity rejected" $false "$_" }
finally { if ($origSeats -match '^\d+$') { Invoke-Sql "UPDATE COURSE_OFFERING SET Max_Seats = $origSeats WHERE Offering_ID = $capOff; COMMIT;" | Out-Null } }

# A4: Successful registration persists (then dropped again, so reruns are identical)
try {
    $s55 = Login "1055" "1234" "STUDENT"
    $offs = (Invoke-Api GET "/api/student/1055/offerings" $null $s55.Session).Json.data
    $activeCourses = @($offs | Where-Object { $_.isEnrolled } | ForEach-Object { "$($_.courseId)|$($_.semester)|$($_.academicYear)" })
    $target = $offs | Where-Object { -not $_.isEnrolled -and $_.availableSeats -gt 0 -and ($activeCourses -notcontains "$($_.courseId)|$($_.semester)|$($_.academicYear)") } |
              Sort-Object offeringId | Select-Object -First 1
    if ($target) {
        $tid = $target.offeringId
        $existed = (Invoke-Sql "SELECT COUNT(*) FROM ENROLLMENT WHERE Student_ID = 1055 AND Offering_ID = $tid;").Trim()
        $r = Invoke-Api POST "/api/student/1055/enroll" @{ offeringId = $tid } $s55.Session
        $list = (Invoke-Api GET "/api/student/1055/enrollments" $null $s55.Session).Json.data
        $found = $list | Where-Object { $_.offeringId -eq $tid -and $_.status -eq "ENROLLED" }
        $drop = Invoke-Api DELETE "/api/student/1055/enrollments/$tid" $null $s55.Session
        if ($existed -eq "0") {
            # The row was created by this test: remove it so the seeded data is unchanged
            Invoke-Sql "DELETE FROM ATTENDANCE WHERE Student_ID = 1055 AND Offering_ID = $tid; DELETE FROM MARKS WHERE Student_ID = 1055 AND Offering_ID = $tid; DELETE FROM ENROLLMENT WHERE Student_ID = 1055 AND Offering_ID = $tid; COMMIT;" | Out-Null
        }
        Record-Test "A4" "Course registration success & persistence" ($r.Status -eq 201 -and $found -and $drop.Status -eq 200) "HTTP $($r.Status): enrolled 1055 in #$tid '$($target.courseName)', verified in list, then dropped (HTTP $($drop.Status)) and test rows removed."
    } else {
        Record-Test "A4" "Course registration success & persistence" $false "No eligible offering found for 1055."
    }
} catch { Record-Test "A4" "Course registration success & persistence" $false "$_" }

# A5/A6: Faculty attendance + marks persist and are visible to the student (original values restored)
try {
    $fac = Login "201" "1234" "FACULTY"; $fSession = $fac.Session
    $before = (Invoke-Sql "SELECT a.Classes_Attended || ',' || m.Continuous_Assessment_Marks || ',' || m.Midterm_Marks || ',' || m.Final_Exam_Marks FROM ATTENDANCE a JOIN MARKS m ON m.Student_ID = a.Student_ID AND m.Offering_ID = a.Offering_ID WHERE a.Student_ID = 1001 AND a.Offering_ID = 401;").Trim().Split(',')
    $r = Invoke-Api PUT "/api/faculty/201/attendance" @{ studentId = 1001; offeringId = 401; classesAttended = 28 } $fSession
    $att = (Invoke-Api GET "/api/student/1001/attendance" $null $studentSession).Json.data | Where-Object { $_.offeringId -eq 401 }
    Record-Test "A5" "Faculty attendance entry visible to student" ($r.Status -eq 200 -and $att.classesAttended -eq 28) "Offering 401: $($att.classesAttended)/$($att.classesHeld) attended ($($att.attendancePercentage)%)."

    $r = Invoke-Api PUT "/api/faculty/201/marks" @{ studentId = 1001; offeringId = 401; continuousAssessmentMarks = 26.5; midtermMarks = 25.0; finalExamMarks = 44.0 } $fSession
    $mk = (Invoke-Api GET "/api/student/1001/marks" $null $studentSession).Json.data | Where-Object { $_.offeringId -eq 401 }
    Record-Test "A6" "Faculty marks entry visible to student (total out of 100)" ($r.Status -eq 200 -and $mk.rawTotal -eq 95.5 -and $mk.totalMarks -eq 86.82 -and $mk.maxTotal -eq 100) "CA 26.5/30 + Mid 25/30 + Final 44/50 = $($mk.rawTotal)/110 -> total $($mk.totalMarks)/$($mk.maxTotal) -> Grade $($mk.gradeCode)."
} catch { Record-Test "A5/A6" "Faculty attendance & marks" $false "$_" }
finally {
    if ($before -and $before.Count -eq 4) {
        Invoke-Sql "UPDATE ATTENDANCE SET Classes_Attended = $($before[0]) WHERE Student_ID = 1001 AND Offering_ID = 401; UPDATE MARKS SET Continuous_Assessment_Marks = $($before[1]), Midterm_Marks = $($before[2]), Final_Exam_Marks = $($before[3]) WHERE Student_ID = 1001 AND Offering_ID = 401; COMMIT;" | Out-Null
    }
}

# A7: Published results carry a grade from the GRADE scale that matches marks / Max_Marks
try {
    $res = (Invoke-Api GET "/api/student/1001/results" $null $studentSession).Json.data
    $scale = @(@{c='S';lo=90},@{c='A';lo=80},@{c='B';lo=70},@{c='C';lo=60},@{c='D';lo=50},@{c='F';lo=0})
    $mismatch = 0
    foreach ($x in $res) {
        $pct = [math]::Round(100.0 * $x.marksObtained / $x.maxMarks, 2)
        $exp = ($scale | Where-Object { $pct -ge $_.lo } | Select-Object -First 1).c
        if ($x.gradeCode -and $x.gradeCode.Trim() -ne $exp) { $mismatch++ }
    }
    Record-Test "A7" "Published results graded per GRADE table" ($res.Count -gt 0 -and $mismatch -eq 0) "$($res.Count) results for 1001; $mismatch grade mismatches."
} catch { Record-Test "A7" "Published results graded per GRADE table" $false "$_" }

# A8: Student without results gets a clean empty list
try {
    $s41 = Login "1041" "1234" "STUDENT"
    $r = Invoke-Api GET "/api/student/1041/results" $null $s41.Session
    Record-Test "A8" "Student without results sees empty state" ($r.Status -eq 200 -and @($r.Json.data).Count -eq 0) "HTTP $($r.Status), $(@($r.Json.data).Count) results."
} catch { Record-Test "A8" "Student without results sees empty state" $false "$_" }

# A9: Re-exam application appears for student and admin
try {
    $adm = Login "admin" "1234" "ADMINISTRATOR"; $adminSession = $adm.Session
    # A result of 1001 that has no application yet: apply -> PENDING in both views -> withdraw
    $target = (Invoke-Api GET "/api/student/1001/results" $null $studentSession).Json.data | Where-Object { -not $_.hasReExamApplied } | Select-Object -First 1
    $apply = Invoke-Api POST "/api/student/1001/re-exams/apply" @{ examResultId = $target.examResultId; reason = "Automated workflow test request" } $studentSession
    $appId = $apply.Json.data.reExamApplicationId
    $app = (Invoke-Api GET "/api/student/1001/re-exams" $null $studentSession).Json.data | Where-Object { $_.reExamApplicationId -eq $appId }
    $inAdmin = (Invoke-Api GET "/api/admin/re-exams" $null $adminSession).Json.data | Where-Object { $_.reExamApplicationId -eq $appId }
    $withdraw = Invoke-Api DELETE "/api/student/1001/re-exams/$appId" $null $studentSession
    Record-Test "A9" "Re-examination submission & tracking" ($apply.Status -eq 201 -and $app.status -eq "PENDING" -and [bool]$inAdmin -and $withdraw.Status -eq 200) "Application #$appId on result #$($target.examResultId): HTTP $($apply.Status), status '$($app.status)' in student and admin views, then withdrawn (HTTP $($withdraw.Status))."
} catch { Record-Test "A9" "Re-examination submission & tracking" $false "$_" }

# A10: Faculty cannot reach another faculty's URL space
$r = Invoke-Api GET "/api/faculty/202/offerings" $null $fSession
Expect "A10" "Cross-faculty access rejected" $r 403

# A11: Admin can list every entity
try {
    $counts = @{}
    foreach ($e in "departments", "programs", "faculty", "students", "offerings", "exams", "results", "re-exams", "accounts", "grades") {
        $counts[$e] = @((Invoke-Api GET "/api/admin/$e" $null $adminSession).Json.data).Count
    }
    $ok = $counts.departments -ge 6 -and $counts.programs -ge 10 -and $counts.faculty -ge 15 -and $counts.students -ge 60 -and $counts.offerings -ge 45 -and $counts.exams -ge 42 -and $counts.accounts -ge 76 -and $counts.grades -eq 6
    Record-Test "A11" "Admin views all entities" $ok (($counts.GetEnumerator() | Sort-Object Name | ForEach-Object { "$($_.Name)=$($_.Value)" }) -join ", ")
} catch { Record-Test "A11" "Admin views all entities" $false "$_" }

# A12/A13: static UI checks (responsive tables, live filters)
$root = Join-Path $PSScriptRoot "..\SmartCampus\src\main\resources\static"
$css = Get-Content (Join-Path $root "css\portal.css") -Raw
Record-Test "A12" "Tables scroll instead of overflowing" (($css -match "\.table-responsive") -and ($css -match "overflow-x:\s*auto")) ".table-responsive with overflow-x: auto present."
$appJs = Get-Content (Join-Path $root "js\app.js") -Raw
$stuJs = Get-Content (Join-Path $root "js\student.js") -Raw
Record-Test "A13" "Table search filter wired" (($appJs -match "filterTable\(") -and ($stuJs -match "UI\.filterTable\(")) "UI.filterTable defined and used."

# A14: Meaningful validation messages
$r1 = Invoke-Api POST "/api/auth/login" @{ username = "9999"; password = "badpassword"; role = "STUDENT" }
$r2 = Invoke-Api PUT "/api/faculty/201/marks" @{ studentId = 1001; offeringId = 401; continuousAssessmentMarks = 35.0; midtermMarks = 20.0; finalExamMarks = 40.0 } $fSession
Record-Test "A14" "Meaningful validation errors" (($r1.Json.message -match "Invalid credentials") -and $r2.Status -eq 400 -and ($r2.Json.message -match "30")) "Login: '$($r1.Json.message)'; Marks (HTTP $($r2.Status)): '$($r2.Json.message)'"

# A15: Errors never leak SQL / stack traces
$r = Invoke-Api POST "/api/admin/departments" @{ departmentName = "Computer Science"; officeLocation = "X" } $adminSession
Record-Test "A15" "Errors are clean JSON without SQL details" ($r.Status -eq 409 -and $r.Json.success -eq $false -and -not ($r.Json.message -match "ORA-|SQL|Exception")) "Duplicate department name -> HTTP $($r.Status): '$($r.Json.message)'"

Write-Host ""
Write-Host "--- PART B: Phase 3 scenarios (Appendix A fixes + CRUD) ---" -ForegroundColor Cyan

$stamp = Get-Date -Format "MMddHHmmss"
$fx = @{}
try {
    # ---- Fixtures through the admin API (Create)
    $fx.dept = (Invoke-Api POST "/api/admin/departments" @{ departmentName = "P3 WF Dept $stamp"; officeLocation = "WF-1" } $adminSession).Json.data.departmentId
    $fx.prog = (Invoke-Api POST "/api/admin/programs" @{ programName = "P3 WF Program $stamp"; degree = "B.Tech"; durationYears = 4; departmentId = $fx.dept } $adminSession).Json.data.programId
    $fx.c1 = (Invoke-Api POST "/api/admin/courses" @{ courseName = "P3 WF Course One"; credits = 4; courseType = "Core"; departmentId = $fx.dept } $adminSession).Json.data.courseId
    $fx.c2 = (Invoke-Api POST "/api/admin/courses" @{ courseName = "P3 WF Course Two"; credits = 3; courseType = "Core"; departmentId = $fx.dept } $adminSession).Json.data.courseId
    $fx.fac = (Invoke-Api POST "/api/admin/faculty" @{ name = "P3 WF Faculty"; email = "p3wf.f.$stamp@test.local"; designation = "Professor"; departmentId = $fx.dept } $adminSession).Json.data.facultyId
    $fx.s1 = (Invoke-Api POST "/api/admin/students" @{ name = "P3 WF Student One"; email = "p3wf.s1.$stamp@test.local"; admissionYear = 2025; programId = $fx.prog } $adminSession).Json.data.studentId
    $fx.s2 = (Invoke-Api POST "/api/admin/students" @{ name = "P3 WF Student Two"; email = "p3wf.s2.$stamp@test.local"; admissionYear = 2025; programId = $fx.prog } $adminSession).Json.data.studentId
    $pw = "P3wf-" + $stamp
    foreach ($a in @(@{ role = "FACULTY"; id = $fx.fac; u = "p3wf-f-$stamp" }, @{ role = "STUDENT"; id = $fx.s1; u = "p3wf-s1-$stamp" }, @{ role = "STUDENT"; id = $fx.s2; u = "p3wf-s2-$stamp" })) {
        Invoke-Api POST "/api/auth/provision" @{ role = $a.role; recordId = $a.id; username = $a.u; password = $pw } $adminSession | Out-Null
    }
    $all = @($fx.dept, $fx.prog, $fx.c1, $fx.c2, $fx.fac, $fx.s1, $fx.s2)
    Record-Test "B1" "CRUD Create: department, program, 2 courses, faculty, 2 students, 3 BCrypt accounts" (-not ($all -contains $null)) "Dept #$($fx.dept), Program #$($fx.prog), Courses #$($fx.c1)/#$($fx.c2), Faculty #$($fx.fac), Students #$($fx.s1)/#$($fx.s2)"

    # ---- F7: semester 10 accepted, 11 rejected
    $o = @{ courseId = $fx.c1; facultyId = $fx.fac; semester = 10; academicYear = "2098-2099"; section = "A"; roomNo = "WF-101"; maxSeats = 1; classesHeld = 10 }
    $r = Invoke-Api POST "/api/admin/offerings" $o $adminSession; $fx.o1A = $r.Json.data.offeringId
    Expect "B2" "Semester 10 offering accepted (NUMBER(2), Finding 7)" $r 201
    $o.section = "B"; $o.maxSeats = 30; $fx.o1B = (Invoke-Api POST "/api/admin/offerings" $o $adminSession).Json.data.offeringId
    $o2 = @{ courseId = $fx.c2; facultyId = $fx.fac; semester = 10; academicYear = "2098-2099"; section = "A"; roomNo = "WF-102"; maxSeats = 30; classesHeld = 10 }
    $fx.o2A = (Invoke-Api POST "/api/admin/offerings" $o2 $adminSession).Json.data.offeringId
    $o2.semester = 11; $o2.section = "Z"
    Expect "B3" "Semester 11 rejected" (Invoke-Api POST "/api/admin/offerings" $o2 $adminSession) 400 "between 1 and 10"

    # ---- Exams in 2099: A 09:00-10:30, B 10:00-11:30 (overlaps A), C 10:30-12:00 (back-to-back with A)
    $mk = { param($off, $s, $e) @{ offeringId = $off; examType = "CAT1"; examDate = "2099-01-10"; startTime = "2099-01-10 $($s):00"; endTime = "2099-01-10 $($e):00"; venue = "WF HALL"; maxMarks = 50 } }
    $fx.eA = (Invoke-Api POST "/api/admin/exams" (& $mk $fx.o1A "09:00" "10:30") $adminSession).Json.data.examId
    $fx.eB = (Invoke-Api POST "/api/admin/exams" (& $mk $fx.o2A "10:00" "11:30") $adminSession).Json.data.examId
    $fx.eC = (Invoke-Api POST "/api/admin/exams" (& $mk $fx.o2A "10:30" "12:00") $adminSession).Json.data.examId

    $s1 = Login "p3wf-s1-$stamp" $pw "STUDENT"; $s2 = Login "p3wf-s2-$stamp" $pw "STUDENT"; $f1 = Login "p3wf-f-$stamp" $pw "FACULTY"
    $stuPath = "/api/student/$($fx.s1)"   # NB: PowerShell names are case-insensitive; keep distinct from $s1

    # ---- F4: course registration rules
    Expect "B4" "Enroll in available offering -> 201" (Invoke-Api POST "$stuPath/enroll" @{ offeringId = $fx.o1A } $s1.Session) 201
    Expect "B5" "Same offering again -> 409" (Invoke-Api POST "$stuPath/enroll" @{ offeringId = $fx.o1A } $s1.Session) 409 "already enrolled"
    Expect "B6" "Other section of same course & term -> 409 (Finding 4)" (Invoke-Api POST "$stuPath/enroll" @{ offeringId = $fx.o1B } $s1.Session) 409 "another section"
    Expect "B7" "Full offering (1 seat) -> 409" (Invoke-Api POST "/api/student/$($fx.s2)/enroll" @{ offeringId = $fx.o1A } $s2.Session) 409 "full capacity"
    Expect "B8" "Unknown offering -> 404" (Invoke-Api POST "$stuPath/enroll" @{ offeringId = 99999999 } $s1.Session) 404
    Invoke-Api POST "$stuPath/enroll" @{ offeringId = $fx.o2A } $s1.Session | Out-Null

    # ---- F5: exam clash rules
    Expect "B9" "Register exam A -> 201" (Invoke-Api POST "$stuPath/exams/register" @{ examId = $fx.eA } $s1.Session) 201
    Expect "B10" "Register exam A again -> 409" (Invoke-Api POST "$stuPath/exams/register" @{ examId = $fx.eA } $s1.Session) 409 "already registered"
    Expect "B11" "Overlapping exam B same date -> 409 (Finding 5)" (Invoke-Api POST "$stuPath/exams/register" @{ examId = $fx.eB } $s1.Session) 409 "clash"
    Expect "B12" "Back-to-back exam C (starts when A ends) -> 201" (Invoke-Api POST "$stuPath/exams/register" @{ examId = $fx.eC } $s1.Session) 201

    # ---- F6: no completion / results before the exam has been held
    Expect "B13" "Mark future exam COMPLETED -> 409 (Finding 6)" (Invoke-Api PATCH "/api/admin/exams/$($fx.eA)/status" @{ status = "COMPLETED" } $adminSession) 409 "cannot be marked COMPLETED"
    $regA = ((Invoke-Api GET "/api/admin/exams/$($fx.eA)/registrations" $null $adminSession).Json.data | Select-Object -First 1).examRegistrationId
    Expect "B14" "Publish result for future exam -> 409 (Finding 6)" (Invoke-Api POST "/api/admin/results" @{ examRegistrationId = $regA; marksObtained = 40 } $adminSession) 409 "future exam"

    # ---- F2: marks 30/30/50 accepted; total scaled to exactly 100/100; ceilings enforced
    $facPath = "/api/faculty/$($fx.fac)"
    $r = Invoke-Api PUT "$facPath/marks" @{ studentId = $fx.s1; offeringId = $fx.o1A; continuousAssessmentMarks = 30; midtermMarks = 30; finalExamMarks = 50 } $f1.Session
    $m = (Invoke-Api GET "$stuPath/marks" $null $s1.Session).Json.data | Where-Object { $_.offeringId -eq $fx.o1A }
    Record-Test "B15" "Marks 30/30/50 accepted; total 100/100, never above 100 (Finding 2)" ($r.Status -eq 200 -and $m.rawTotal -eq 110 -and $m.totalMarks -eq 100 -and $m.maxTotal -eq 100) "HTTP $($r.Status); raw $($m.rawTotal)/110 -> total $($m.totalMarks)/$($m.maxTotal), grade $($m.gradeCode)"
    Expect "B16" "Final 51 (> 50) -> 400" (Invoke-Api PUT "$facPath/marks" @{ studentId = $fx.s1; offeringId = $fx.o1A; continuousAssessmentMarks = 10; midtermMarks = 10; finalExamMarks = 51 } $f1.Session) 400
    Expect "B17" "Attendance above Classes_Held -> 400" (Invoke-Api PUT "$facPath/attendance" @{ studentId = $fx.s1; offeringId = $fx.o1A; classesAttended = 11 } $f1.Session) 400 "cannot exceed"
    Expect "B18" "Faculty 201 updating another faculty's offering -> 403" (Invoke-Api PUT "/api/faculty/201/attendance" @{ studentId = $fx.s1; offeringId = $fx.o1A; classesAttended = 1 } $fSession) 403

    # ---- Access control
    Expect "B19" "No session -> 401" (Invoke-Api GET "/api/admin/stats") 401
    Expect "B20" "Student reading another student's profile -> 403" (Invoke-Api GET "/api/student/$($fx.s2)/profile" $null $s1.Session) 403
    Expect "B21" "Student calling admin API -> 403" (Invoke-Api GET "/api/admin/stats" $null $s1.Session) 403

    # ---- CRUD Update / Delete with FK protection
    Expect "B22" "CRUD Update: rename department -> 200" (Invoke-Api PUT "/api/admin/departments/$($fx.dept)" @{ departmentName = "P3 WF Dept $stamp (renamed)"; officeLocation = "WF-2" } $adminSession) 200
    Expect "B23" "Delete department with children -> 409" (Invoke-Api DELETE "/api/admin/departments/$($fx.dept)" $null $adminSession) 409 "cannot be deleted"
    Expect "B24" "Delete exam with registrations -> 409" (Invoke-Api DELETE "/api/admin/exams/$($fx.eA)" $null $adminSession) 409
    Expect "B25" "Delete student with enrollments -> 409" (Invoke-Api DELETE "/api/admin/students/$($fx.s1)" $null $adminSession) 409
    Expect "B26" "Delete unknown department -> 404" (Invoke-Api DELETE "/api/admin/departments/99999999" $null $adminSession) 404
    Expect "B27" "Delete unreferenced exam B -> 200" (Invoke-Api DELETE "/api/admin/exams/$($fx.eB)" $null $adminSession) 200
    Expect "B28" "Student drops course (logical DROP) -> 200" (Invoke-Api DELETE "$stuPath/enrollments/$($fx.o2A)" $null $s1.Session) 200
    $st = (Invoke-Sql "SELECT Status FROM ENROLLMENT WHERE Student_ID = $($fx.s1) AND Offering_ID = $($fx.o2A);").Trim()
    $rg = (Invoke-Sql "SELECT Status FROM EXAM_REGISTRATION WHERE Student_ID = $($fx.s1) AND Exam_ID = $($fx.eC);").Trim()
    Record-Test "B29" "Dropped row kept as DROPPED and its exam registration CANCELLED" ($st -eq "DROPPED" -and $rg -eq "CANCELLED") "ENROLLMENT.Status=$st, EXAM_REGISTRATION.Status=$rg"
} catch {
    Record-Test "B-ERR" "Phase 3 scenario block" $false "$_"
} finally {
    # ---- Cleanup of fixture rows (only IDs created above)
    try {
        $sql = @()
        $exams = (@($fx.eA, $fx.eB, $fx.eC) | Where-Object { $_ }) -join ","
        $offs = (@($fx.o1A, $fx.o1B, $fx.o2A) | Where-Object { $_ }) -join ","
        $stus = (@($fx.s1, $fx.s2) | Where-Object { $_ }) -join ","
        if ($exams) { $sql += "DELETE FROM EXAM_RESULT WHERE Exam_Registration_ID IN (SELECT Exam_Registration_ID FROM EXAM_REGISTRATION WHERE Exam_ID IN ($exams));"; $sql += "DELETE FROM EXAM_REGISTRATION WHERE Exam_ID IN ($exams);" }
        if ($offs) { foreach ($t in "ATTENDANCE", "MARKS", "ENROLLMENT") { $sql += "DELETE FROM $t WHERE Offering_ID IN ($offs);" } }
        if ($stus) { $sql += "DELETE FROM USER_ACCOUNT WHERE Student_ID IN ($stus);" }
        if ($fx.fac) { $sql += "DELETE FROM USER_ACCOUNT WHERE Faculty_ID = $($fx.fac);" }
        $sql += "COMMIT;"
        Invoke-Sql ($sql -join "`n") | Out-Null
        # Remaining parents go through the API (CRUD Delete), children first
        $codes = @()
        foreach ($e in @($fx.eA, $fx.eC)) { if ($e) { $codes += (Invoke-Api DELETE "/api/admin/exams/$e" $null $adminSession).Status } }
        foreach ($o in @($fx.o1A, $fx.o1B, $fx.o2A)) { if ($o) { $codes += (Invoke-Api DELETE "/api/admin/offerings/$o" $null $adminSession).Status } }
        foreach ($s in @($fx.s1, $fx.s2)) { if ($s) { $codes += (Invoke-Api DELETE "/api/admin/students/$s" $null $adminSession).Status } }
        if ($fx.fac) { $codes += (Invoke-Api DELETE "/api/admin/faculty/$($fx.fac)" $null $adminSession).Status }
        foreach ($c in @($fx.c1, $fx.c2)) { if ($c) { $codes += (Invoke-Api DELETE "/api/admin/courses/$c" $null $adminSession).Status } }
        if ($fx.prog) { $codes += (Invoke-Api DELETE "/api/admin/programs/$($fx.prog)" $null $adminSession).Status }
        if ($fx.dept) { $codes += (Invoke-Api DELETE "/api/admin/departments/$($fx.dept)" $null $adminSession).Status }
        $allOk = ($codes.Count -gt 0) -and (@($codes | Where-Object { $_ -ne 200 }).Count -eq 0)
        Record-Test "B30" "CRUD Delete: fixtures removed via API once children are gone" $allOk "DELETE status codes: $($codes -join ', ')"
    } catch { Record-Test "B30" "CRUD Delete cleanup" $false "$_" }
}

# B31: Oracle-level constraint probes (rolled back) from database/phase3/03_verify_post_migration.sql
try {
    $probe = Get-Content (Join-Path $PSScriptRoot "..\database\phase3\03_verify_post_migration.sql") -Raw
    $out = Invoke-Sql $probe
    $line = ($out -split "`n" | Where-Object { $_ -match "CONSTRAINT PROBES" }) | Select-Object -First 1
    Record-Test "B31" "Oracle CHECK/UNIQUE/FK probes (semester, 5 status columns, marks ceilings)" ($line -match "0 failed") "$line"
} catch { Record-Test "B31" "Oracle constraint probes" $false "$_" }

$total = $script:passCount + $script:failCount
Write-Host "=========================================================================" -ForegroundColor Cyan
Write-Host " VERIFICATION SUMMARY: $($script:passCount) / $total PASSED, $($script:failCount) FAILED" -ForegroundColor $(if ($script:failCount -eq 0) { "Green" } else { "Red" })
Write-Host "=========================================================================" -ForegroundColor Cyan
if ($script:failCount -eq 0) { exit 0 } else { exit 1 }
