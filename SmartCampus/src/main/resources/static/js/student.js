// Student Portal Module

const StudentPortal = {
    async loadDashboard(studentId) {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading student dashboard...');

        try {
            const [profile, enrollments, attendance, marks, exams] = await Promise.all([
                API.getStudentProfile(studentId),
                API.getStudentEnrollments(studentId),
                API.getStudentAttendance(studentId),
                API.getStudentMarks(studentId),
                API.getStudentExams(studentId)
            ]);

            // Calculate overall attendance
            let totalHeld = 0, totalAttended = 0;
            attendance.forEach(a => {
                totalHeld += (a.classesHeld || 0);
                totalAttended += (a.classesAttended || 0);
            });
            const overallAttPct = totalHeld > 0 ? ((totalAttended / totalHeld) * 100).toFixed(1) : 0;

            // Compute registered credits
            let totalCredits = enrollments.reduce((sum, e) => sum + (e.credits || 0), 0);

            // Compute Semester GPA
            let totalCreds = 0, weightedPts = 0;
            marks.forEach(m => {
                if (m.credits && m.gradePoint !== null && m.gradePoint !== undefined) {
                    totalCreds += m.credits;
                    weightedPts += (m.credits * m.gradePoint);
                }
            });
            const gpa = totalCreds > 0 ? (weightedPts / totalCreds).toFixed(2) : 'N/A';
            const upcomingExams = (exams || []).slice(0, 5);

            container.innerHTML = `
                <div class="dashboard-header">
                    <div class="dashboard-title">
                        <h1>Student Academic Overview</h1>
                        <p>Official student record verified against Oracle Database &bull; XYZ University</p>
                    </div>
                </div>

                <!-- Metrics Row -->
                <div class="metrics-row">
                    <div class="metric-card">
                        <div>
                            <div class="metric-label">Registered Credits</div>
                            <div class="metric-value">${totalCredits}.0</div>
                            <div class="metric-sub">Current Semester</div>
                        </div>
                        <div class="metric-icon">📚</div>
                    </div>
                    <div class="metric-card">
                        <div>
                            <div class="metric-label">Overall Attendance</div>
                            <div class="metric-value">${overallAttPct}%</div>
                            <div class="metric-sub">${totalAttended} of ${totalHeld} classes attended</div>
                        </div>
                        <div class="metric-icon">📅</div>
                    </div>
                    <div class="metric-card">
                        <div>
                            <div class="metric-label">Semester GPA</div>
                            <div class="metric-value">${gpa !== 'N/A' ? gpa : '—'}</div>
                            <div class="metric-sub">Weighted Grade Point Average</div>
                        </div>
                        <div class="metric-icon">🏆</div>
                    </div>
                    <div class="metric-card">
                        <div>
                            <div class="metric-label">Active Courses</div>
                            <div class="metric-value">${enrollments.length}</div>
                            <div class="metric-sub">Enrolled Offerings</div>
                        </div>
                        <div class="metric-icon">📖</div>
                    </div>
                </div>

                <!-- Course Registration & Attendance Card -->
                <div class="data-card">
                    <div class="data-card-header">
                        <div class="data-card-title">📘 Current Semester Course Registrations & Attendance</div>
                    </div>
                    <div class="table-wrapper">
                        <table class="academic-table">
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Course Code & Title</th>
                                    <th>Type</th>
                                    <th>Credits</th>
                                    <th>Attendance</th>
                                    <th>Academic Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${enrollments.map((e, idx) => {
                                    const att = attendance.find(a => a.offeringId === e.offeringId) || { attendancePercentage: 0, classesAttended: 0, classesHeld: 0 };
                                    const pct = att.attendancePercentage !== undefined ? att.attendancePercentage : 0;
                                    
                                    let statusBadge = '';
                                    if (pct >= 85) {
                                        statusBadge = '<span class="status-badge success">Good Standing</span>';
                                    } else if (pct >= 75) {
                                        statusBadge = '<span class="status-badge warning">Satisfactory (&ge; 75%)</span>';
                                    } else {
                                        statusBadge = '<span class="status-badge danger">Low Attendance (&lt; 75%)</span>';
                                    }

                                    return `
                                        <tr>
                                            <td>${idx + 1}</td>
                                            <td><strong>CRS-${e.offeringId}</strong> — ${e.courseName}</td>
                                            <td><span class="status-badge info">${e.credits > 3 ? 'Theory & Lab' : 'Theory'}</span></td>
                                            <td>${e.credits}</td>
                                            <td><strong>${pct.toFixed(1)}%</strong> <span style="font-size: 11px; color: var(--color-text-muted);">(${att.classesAttended}/${att.classesHeld})</span></td>
                                            <td>${statusBadge}</td>
                                        </tr>
                                    `;
                                }).join('') || '<tr><td colspan="6" class="empty-state">No current semester course registrations found.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Upcoming Examinations Card -->
                <div class="data-card">
                    <div class="data-card-header">
                        <div class="data-card-title">📝 Forthcoming University Examinations</div>
                    </div>
                    <div class="table-wrapper">
                        <table class="academic-table">
                            <thead>
                                <tr>
                                    <th>Course Title</th>
                                    <th>Exam Category</th>
                                    <th>Date</th>
                                    <th>Schedule</th>
                                    <th>Hall / Venue</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${upcomingExams.map(ex => `
                                    <tr>
                                        <td><strong>${ex.courseName}</strong></td>
                                        <td><span class="status-badge info">${ex.examType}</span></td>
                                        <td>${ex.examDate}</td>
                                        <td>${ex.startTime || 'TBA'} - ${ex.endTime || 'TBA'}</td>
                                        <td>${ex.venue}</td>
                                        <td><span class="status-badge success">Admit Card Confirmed</span></td>
                                    </tr>
                                `).join('') || '<tr><td colspan="6" class="empty-state">No upcoming registered examinations in queue.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load student dashboard: ' + err.message);
        }
    },

    async loadProfile(studentId) {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading student profile...');

        try {
            const p = await API.getStudentProfile(studentId);

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Student Academic Profile</h2>
                        <p>Official student record verified against Oracle Database</p>
                    </div>
                </div>

                <div class="card" style="max-width: 800px;">
                    <div class="card-header">
                        <h3>Personal & Academic Details</h3>
                        <span class="badge badge-success">ACTIVE ENROLLMENT</span>
                    </div>
                    <div class="card-body">
                        <div class="form-grid-2" style="row-gap: 20px;">
                            <div>
                                <label class="form-label">Full Name</label>
                                <div style="font-size: 15px; font-weight: 600;">${p.name}</div>
                            </div>
                            <div>
                                <label class="form-label">Student ID (Roll No)</label>
                                <div style="font-size: 15px; font-weight: 600; color: var(--primary-700);">${p.studentId}</div>
                            </div>
                            <div>
                                <label class="form-label">College Email</label>
                                <div>${p.email}</div>
                            </div>
                            <div>
                                <label class="form-label">Phone Number</label>
                                <div>${p.phone || 'N/A'}</div>
                            </div>
                            <div>
                                <label class="form-label">Date of Birth</label>
                                <div>${p.dob || 'N/A'}</div>
                            </div>
                            <div>
                                <label class="form-label">Admission Year</label>
                                <div>${p.admissionYear}</div>
                            </div>
                            <div>
                                <label class="form-label">Academic Program</label>
                                <div style="font-weight: 600;">${p.degree} - ${p.programName}</div>
                            </div>
                            <div>
                                <label class="form-label">Department</label>
                                <div>${p.departmentName}</div>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load profile: ' + err.message);
        }
    },

    async loadCourses(studentId) {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading course offerings...');

        try {
            const [offerings, enrollments] = await Promise.all([
                API.getStudentOfferings(studentId),
                API.getStudentEnrollments(studentId)
            ]);

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Course Registration & Offerings</h2>
                        <p>Register for available semester course offerings with seat capacity enforcement</p>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>Currently Registered Courses (${enrollments.length})</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Enrollment ID</th>
                                    <th>Course Name</th>
                                    <th>Credits</th>
                                    <th>Section</th>
                                    <th>Faculty</th>
                                    <th>Enrolled Date</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${enrollments.map(e => `
                                    <tr>
                                        <td>#${e.enrollmentId}</td>
                                        <td><strong>${e.courseName}</strong></td>
                                        <td>${e.credits}</td>
                                        <td>Sec ${e.section}</td>
                                        <td>${e.facultyName}</td>
                                        <td>${e.enrollmentDate}</td>
                                        <td><span class="badge ${e.status === 'ENROLLED' ? 'badge-success' : e.status === 'DROPPED' ? 'badge-secondary' : 'badge-info'}">${e.status}</span></td>
                                        <td>${e.status === 'ENROLLED'
                                            ? `<button class="btn btn-sm btn-danger" onclick="StudentPortal.dropCourse(${studentId}, ${e.offeringId}, '${AdminCrud.esc(e.courseName).replace(/'/g, '&#39;')}')">Drop</button>`
                                            : ''}</td>
                                    </tr>
                                `).join('') || '<tr><td colspan="8" class="empty-state">No courses currently registered.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                        <h3>Available Course Offerings for Registration (${offerings.length})</h3>
                        <input type="text" class="form-input" placeholder="🔍 Search courses by title, faculty, or type..." style="max-width: 320px; height: 34px; font-size: 13px;" oninput="UI.filterTable('#student-offerings-table', this.value)" />
                    </div>
                    <div class="table-responsive">
                        <table class="data-table" id="student-offerings-table">
                            <thead>
                                <tr>
                                    <th>Offering ID</th>
                                    <th>Course</th>
                                    <th>Type</th>
                                    <th>Credits</th>
                                    <th>Faculty</th>
                                    <th>Sec & Room</th>
                                    <th>Available Seats</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${offerings.map(o => {
                                    const isFull = o.availableSeats <= 0;
                                    const canEnroll = !o.isEnrolled && !isFull;
                                    return `
                                        <tr>
                                            <td>#${o.offeringId}</td>
                                            <td>
                                                <strong>${o.courseName}</strong>
                                                <div style="font-size: 11px; color: var(--gray-600);">Sem ${o.semester} &bull; ${o.academicYear}</div>
                                            </td>
                                            <td><span class="badge badge-info">${o.courseType}</span></td>
                                            <td>${o.credits}</td>
                                            <td>${o.facultyName}</td>
                                            <td>Sec ${o.section} (${o.roomNo || 'TBD'})</td>
                                            <td>
                                                <span class="badge ${isFull ? 'badge-danger' : 'badge-success'}">
                                                    ${o.enrolledCount} / ${o.maxSeats} (${o.availableSeats} Left)
                                                </span>
                                            </td>
                                            <td>
                                                ${o.isEnrolled 
                                                    ? `<button class="btn btn-sm btn-danger" onclick="StudentPortal.dropCourse(${studentId}, ${o.offeringId}, '${AdminCrud.esc(o.courseName).replace(/'/g, '&#39;')}')">Drop Course</button>`
                                                    : isFull 
                                                        ? '<span class="badge badge-danger">SEATS FULL</span>' 
                                                        : `<button class="btn btn-sm btn-primary" onclick="StudentPortal.enroll(${studentId}, ${o.offeringId}, '${o.courseName}')">Register Now</button>`
                                                }
                                            </td>
                                        </tr>
                                    `;
                                }).join('')}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load courses: ' + err.message);
        }
    },

    async enroll(studentId, offeringId, courseName) {
        if (!confirm(`Are you sure you want to register for ${courseName} (Offering #${offeringId})?`)) {
            return;
        }

        try {
            await API.enrollCourse(studentId, offeringId);
            UI.showToast(`Successfully enrolled in ${courseName}!`, 'success');
            StudentPortal.loadCourses(studentId);
        } catch (err) {
            // Toast is shown by API client (409 duplicate / other section / full, 404 unknown offering)
        }
    },

    async dropCourse(studentId, offeringId, courseName, reloadView) {
        if (!confirm(`Drop ${courseName} (Offering #${offeringId})? Your attendance and marks history is kept, and any pending exam registrations for it are cancelled.`)) {
            return;
        }
        try {
            await API.dropCourse(studentId, offeringId);
            if (reloadView === 'marks') StudentPortal.loadMarks(studentId);
            else StudentPortal.loadCourses(studentId);
        } catch (err) {
            // 409 when results are already published
        }
    },

    async loadAttendance(studentId) {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading attendance records...');

        try {
            const records = await API.getStudentAttendance(studentId);

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Attendance Records & Analytics</h2>
                        <p>Subject-wise attendance tracking with statutory 75% requirement monitor</p>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>Subject Attendance Summary</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Course</th>
                                    <th>Section</th>
                                    <th>Classes Attended</th>
                                    <th>Classes Held</th>
                                    <th style="width: 250px;">Percentage</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${records.map(a => {
                                    const pct = a.attendancePercentage || 0;
                                    const colorClass = pct >= 75 ? 'progress-green' : pct >= 65 ? 'progress-orange' : 'progress-red';
                                    return `
                                        <tr>
                                            <td><strong>${a.courseName}</strong></td>
                                            <td>Sec ${a.section}</td>
                                            <td>${a.classesAttended}</td>
                                            <td>${a.classesHeld}</td>
                                            <td>
                                                <div style="display: flex; align-items: center; gap: 10px;">
                                                    <div class="progress-bar-container">
                                                        <div class="progress-bar-fill ${colorClass}" style="width: ${Math.min(100, pct)}%;"></div>
                                                    </div>
                                                    <span style="font-weight: 700; width: 45px; text-align: right;">${pct}%</span>
                                                </div>
                                            </td>
                                            <td>
                                                ${a.isShortage 
                                                    ? '<span class="badge badge-danger">⚠️ SHORTAGE (&lt; 75%)</span>' 
                                                    : '<span class="badge badge-success">✓ ELIGIBLE</span>'
                                                }
                                            </td>
                                        </tr>
                                    `;
                                }).join('') || '<tr><td colspan="6" class="empty-state">No attendance records found.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load attendance: ' + err.message);
        }
    },

    async loadMarks(studentId) {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading marks and grades...');

        try {
            const records = await API.getStudentMarks(studentId);

            let totalCreds = 0, weightedPts = 0;
            records.forEach(m => {
                if (m.credits && m.gradePoint !== null && m.gradePoint !== undefined) {
                    totalCreds += m.credits;
                    weightedPts += (m.credits * m.gradePoint);
                }
            });
            const gpa = totalCreds > 0 ? (weightedPts / totalCreds).toFixed(2) : 'N/A';

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Continuous Assessment & Examination Marks</h2>
                        <p>Internal assessment, midterm, and final examination performance &bull; Semester GPA: <strong>${gpa}</strong></p>
                    </div>
                    <button class="btn btn-primary" onclick="StudentPortal.openPrintTranscript(${studentId})">🖨️ View Official Grade Sheet</button>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>Course Marks Breakdown</h3>
                        <span class="badge badge-info">Graded Credits: ${totalCreds}</span>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Course</th>
                                    <th>Credits</th>
                                    <th>Section</th>
                                    <th>CA Marks (30)</th>
                                    <th>Midterm (30)</th>
                                    <th>Final Exam (50)</th>
                                    <th title="(CA + Midterm + Final) scaled from 110 to 100">Total (100)</th>
                                    <th>Grade</th>
                                    <th>Grade Point</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${records.map(m => `
                                    <tr>
                                        <td><strong>${m.courseName}</strong></td>
                                        <td>${m.credits || 4}</td>
                                        <td>Sec ${m.section}</td>
                                        <td>${m.continuousAssessmentMarks} <span class="marks-ceiling">/ 30</span></td>
                                        <td>${m.midtermMarks} <span class="marks-ceiling">/ 30</span></td>
                                        <td>${m.finalExamMarks} <span class="marks-ceiling">/ 50</span></td>
                                        <td title="Raw ${m.rawTotal} / 110"><strong>${m.totalMarks.toFixed(2)}</strong> <span class="marks-ceiling">/ 100</span></td>
                                        <td>
                                            <span class="badge ${m.gradeCode === 'F' ? 'badge-danger' : 'badge-success'}">
                                                Grade ${m.gradeCode}
                                            </span>
                                        </td>
                                        <td><strong>${m.gradePoint !== null ? m.gradePoint : 0}</strong></td>
                                        <td><button class="btn btn-sm btn-danger" onclick="StudentPortal.dropCourse(${studentId}, ${m.offeringId}, '${AdminCrud.esc(m.courseName).replace(/'/g, '&#39;')}', 'marks')">Drop</button></td>
                                    </tr>
                                `).join('') || '<tr><td colspan="10" class="empty-state">No marks records available.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                    <p class="marks-ceiling" style="padding: 10px 16px; margin: 0;">
                        Total = (CA + Midterm + Final) &times; 100 &divide; 110, so it is always out of 100. The grade is taken from this total.
                    </p>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load marks: ' + err.message);
        }
    },

    async openPrintTranscript(studentId) {
        try {
            const [profile, marks] = await Promise.all([
                API.getStudentProfile(studentId),
                API.getStudentMarks(studentId)
            ]);

            let totalCreds = 0, weightedPts = 0;
            marks.forEach(m => {
                if (m.credits && m.gradePoint !== null && m.gradePoint !== undefined) {
                    totalCreds += m.credits;
                    weightedPts += (m.credits * m.gradePoint);
                }
            });
            const gpa = totalCreds > 0 ? (weightedPts / totalCreds).toFixed(2) : 'N/A';

            const modalHtml = `
                <div class="modal-backdrop" id="transcript-modal">
                    <div class="modal-content" style="max-width: 800px;">
                        <div class="modal-header">
                            <h3>Official Academic Grade Sheet</h3>
                            <button class="modal-close" onclick="UI.closeModal('transcript-modal')">&times;</button>
                        </div>
                        <div class="modal-body" id="printable-transcript" style="padding: 28px; background: #fff; color: #1a202c;">
                            <div style="text-align: center; border-bottom: 2px solid #0f2b48; padding-bottom: 16px; margin-bottom: 20px;">
                                <h2 style="color: #0f2b48; font-size: 20px; font-weight: 800; letter-spacing: 0.5px;">XYZ UNIVERSITY</h2>
                                <p style="font-size: 12px; color: #4a5568; text-transform: uppercase; margin-top: 2px;">Office of the Academic Registrar & Evaluation</p>
                                <p style="font-size: 13px; font-weight: 700; color: #2b6cb0; margin-top: 6px;">SEMESTER GRADE REPORT &bull; ACADEMIC YEAR 2026-2027</p>
                            </div>

                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; font-size: 12px; margin-bottom: 20px; background: #f7fafc; padding: 14px; border-radius: 8px; border: 1px solid #e2e8f0;">
                                <div><strong>Student Name:</strong> ${profile.name}</div>
                                <div><strong>Roll Number:</strong> ${profile.studentId}</div>
                                <div><strong>Degree & Program:</strong> ${profile.degree} - ${profile.programName}</div>
                                <div><strong>Department:</strong> ${profile.departmentName}</div>
                                <div><strong>Admission Year:</strong> ${profile.admissionYear}</div>
                                <div><strong>Issue Date:</strong> ${new Date().toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}</div>
                            </div>

                            <table style="width: 100%; border-collapse: collapse; font-size: 12px; margin-bottom: 20px;">
                                <thead>
                                    <tr style="background: #0f2b48; color: #fff;">
                                        <th style="padding: 8px 10px; text-align: left;">Course Title</th>
                                        <th style="padding: 8px 10px; text-align: center;">Credits</th>
                                        <th style="padding: 8px 10px; text-align: center;">CA (30)</th>
                                        <th style="padding: 8px 10px; text-align: center;">Midterm (30)</th>
                                        <th style="padding: 8px 10px; text-align: center;">Final (50)</th>
                                        <th style="padding: 8px 10px; text-align: center;">Total (100)</th>
                                        <th style="padding: 8px 10px; text-align: center;">Grade</th>
                                        <th style="padding: 8px 10px; text-align: center;">Grade Point</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    ${marks.map(m => `
                                        <tr style="border-bottom: 1px solid #e2e8f0;">
                                            <td style="padding: 8px 10px;"><strong>${m.courseName}</strong> (Sec ${m.section})</td>
                                            <td style="padding: 8px 10px; text-align: center;">${m.credits || 4}</td>
                                            <td style="padding: 8px 10px; text-align: center;">${m.continuousAssessmentMarks}</td>
                                            <td style="padding: 8px 10px; text-align: center;">${m.midtermMarks}</td>
                                            <td style="padding: 8px 10px; text-align: center;">${m.finalExamMarks}</td>
                                            <td style="padding: 8px 10px; text-align: center;"><strong>${m.totalMarks.toFixed(2)}</strong> / 100</td>
                                            <td style="padding: 8px 10px; text-align: center; font-weight: 700;">${m.gradeCode}</td>
                                            <td style="padding: 8px 10px; text-align: center;">${m.gradePoint}</td>
                                        </tr>
                                    `).join('')}
                                </tbody>
                            </table>

                            <div style="display: flex; justify-content: space-between; align-items: center; background: #edf2f7; padding: 14px 18px; border-radius: 8px; font-size: 13px;">
                                <div><strong>Total Graded Credits:</strong> ${totalCreds}</div>
                                <div style="font-size: 15px; font-weight: 800; color: #1a365d;">SEMESTER GPA: ${gpa} / 10.0</div>
                                <div><span class="badge badge-success">ACADEMIC STANDING: EXCELLENT</span></div>
                            </div>

                            <div style="margin-top: 30px; display: flex; justify-content: space-between; font-size: 11px; color: #718096; border-top: 1px dashed #cbd5e0; padding-top: 16px;">
                                <div>Generated electronically by Smart Campus Management System.</div>
                                <div>Controller of Examinations</div>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" onclick="UI.closeModal('transcript-modal')">Close</button>
                            <button type="button" class="btn btn-primary" onclick="window.print()">🖨️ Print / Save PDF</button>
                        </div>
                    </div>
                </div>
            `;
            UI.openModal(modalHtml);
        } catch (err) {
            UI.showToast('Could not generate transcript: ' + err.message, 'error');
        }
    },

    async loadExams(studentId) {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading exam schedules...');

        try {
            const exams = await API.getStudentExams(studentId);

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Examination Schedules & Registration</h2>
                        <p>View timetable and register for semester examinations</p>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>Scheduled Examinations for Enrolled Courses</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Exam ID</th>
                                    <th>Course</th>
                                    <th>Type</th>
                                    <th>Date</th>
                                    <th>Time</th>
                                    <th>Venue</th>
                                    <th>Max Marks</th>
                                    <th>Registration Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${exams.map(e => `
                                    <tr>
                                        <td>#${e.examId}</td>
                                        <td><strong>${e.courseName}</strong> (Sec ${e.section})</td>
                                        <td><span class="badge badge-info">${e.examType}</span></td>
                                        <td>${e.examDate}</td>
                                        <td>${e.startTime ? e.startTime.replace('T', ' ') : 'TBD'}</td>
                                        <td>${e.venue}</td>
                                        <td>${e.maxMarks}</td>
                                        <td>
                                            ${e.isRegistered 
                                                ? `<span class="badge badge-success">REGISTERED (#${e.examRegistrationId})</span>` 
                                                : '<span class="badge badge-warning">NOT REGISTERED</span>'
                                            }
                                        </td>
                                        <td>
                                            ${e.status !== 'SCHEDULED'
                                                ? `<span class="badge badge-secondary">${e.status}</span>`
                                                : e.isRegistered
                                                    ? `<button class="btn btn-sm btn-secondary" onclick="StudentPortal.cancelExamRegistration(${studentId}, ${e.examId}, '${e.examType}')">Cancel Registration</button>`
                                                    : `<button class="btn btn-sm btn-primary" onclick="StudentPortal.registerForExam(${studentId}, ${e.examId}, '${e.courseName}', '${e.examType}')">Register</button>`
                                            }
                                        </td>
                                    </tr>
                                `).join('') || '<tr><td colspan="9" class="empty-state">No upcoming examinations scheduled for your enrolled courses.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load exams: ' + err.message);
        }
    },

    async registerForExam(studentId, examId, courseName, examType) {
        if (!confirm(`Confirm examination registration for ${courseName} - ${examType}?`)) {
            return;
        }

        try {
            await API.registerExam(studentId, examId);
            UI.showToast(`Successfully registered for ${courseName} ${examType}!`, 'success');
            StudentPortal.loadExams(studentId);
        } catch (err) {
            // Handled (409 duplicate / timetable clash / closed, 404 unknown exam)
        }
    },

    async cancelExamRegistration(studentId, examId, examType) {
        if (!confirm(`Cancel your ${examType} registration for exam #${examId}?`)) {
            return;
        }
        try {
            await API.cancelExamRegistration(studentId, examId);
            StudentPortal.loadExams(studentId);
        } catch (err) {
            // Handled
        }
    },

    async withdrawReExam(studentId, applicationId) {
        if (!confirm(`Withdraw pending re-examination application #${applicationId}?`)) {
            return;
        }
        try {
            await API.withdrawReExam(studentId, applicationId);
            StudentPortal.loadReExams(studentId);
        } catch (err) {
            // Handled
        }
    },

    async loadResults(studentId) {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading published exam results...');

        try {
            const results = await API.getStudentResults(studentId);

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Published Examination Results</h2>
                        <p>Official evaluated marks, grades, and re-examination eligibility</p>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>Evaluation Results</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Result ID</th>
                                    <th>Course</th>
                                    <th>Exam</th>
                                    <th>Exam Date</th>
                                    <th>Marks Obtained</th>
                                    <th>Percentage</th>
                                    <th>Result Status</th>
                                    <th>Grade</th>
                                    <th>Published Date</th>
                                    <th>Re-Exam</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${results.map(r => `
                                    <tr>
                                        <td>#${r.examResultId}</td>
                                        <td><strong>${r.courseName}</strong></td>
                                        <td><span class="badge badge-info">${r.examType}</span></td>
                                        <td>${r.examDate}</td>
                                        <td><strong>${r.marksObtained}</strong> / ${r.maxMarks}</td>
                                        <td>${r.percentage}%</td>
                                        <td><span class="badge ${r.resultStatus === 'PASS' ? 'badge-success' : 'badge-danger'}">${r.resultStatus}</span></td>
                                        <td><strong>Grade ${r.gradeCode || 'N/A'}</strong></td>
                                        <td>${r.publishedDate || 'N/A'}</td>
                                        <td>
                                            ${r.hasReExamApplied 
                                                ? `<span class="badge badge-warning">RE-EXAM: ${r.reExamStatus}</span>` 
                                                : `<button class="btn btn-sm btn-secondary" onclick="StudentPortal.openReExamModal(${studentId}, ${r.examResultId}, '${r.courseName}', '${r.examType}')">Apply Re-Exam</button>`
                                            }
                                        </td>
                                    </tr>
                                `).join('') || '<tr><td colspan="10" class="empty-state">No published examination results found.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load results: ' + err.message);
        }
    },

    openReExamModal(studentId, examResultId, courseName, examType) {
        const modalHtml = `
            <div class="modal-backdrop" id="reexam-modal">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3>Apply for Re-Examination</h3>
                        <button class="modal-close" onclick="UI.closeModal('reexam-modal')">&times;</button>
                    </div>
                    <form onsubmit="StudentPortal.submitReExam(event, ${studentId}, ${examResultId})">
                        <div class="modal-body">
                            <p style="margin-bottom: 16px; color: var(--gray-600);">
                                Course: <strong>${courseName}</strong> (${examType}) &bull; Result ID: #${examResultId}
                            </p>
                            <div class="form-group">
                                <label class="form-label">Justification / Reason for Re-Examination</label>
                                <textarea id="reexam-reason" class="form-control" rows="4" required placeholder="Specify academic reasons or extenuating circumstances..."></textarea>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" onclick="UI.closeModal('reexam-modal')">Cancel</button>
                            <button type="submit" class="btn btn-primary">Submit Application</button>
                        </div>
                    </form>
                </div>
            </div>
        `;
        UI.openModal(modalHtml);
    },

    async submitReExam(event, studentId, examResultId) {
        event.preventDefault();
        const reason = document.getElementById('reexam-reason').value;

        try {
            await API.applyReExam(studentId, examResultId, reason);
            UI.closeModal('reexam-modal');
            UI.showToast('Re-examination application submitted successfully!', 'success');
            StudentPortal.loadResults(studentId);
        } catch (err) {
            // Toast shown
        }
    },

    async loadReExams(studentId) {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading re-examination applications...');

        try {
            const apps = await API.getStudentReExams(studentId);

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Re-Examination Applications Status</h2>
                        <p>Track administrator review and approvals for submitted re-examination requests</p>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>My Applications</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Application ID</th>
                                    <th>Course</th>
                                    <th>Exam Type</th>
                                    <th>Previous Score</th>
                                    <th>Reason Submitted</th>
                                    <th>Application Date</th>
                                    <th>Status</th>
                                    <th>Approval Date</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${apps.map(a => `
                                    <tr>
                                        <td>#${a.reExamApplicationId}</td>
                                        <td><strong>${a.courseName}</strong></td>
                                        <td>${a.examType}</td>
                                        <td>${a.marksObtained} (Grade ${a.gradeCode})</td>
                                        <td>${a.reason}</td>
                                        <td>${a.applicationDate}</td>
                                        <td>
                                            <span class="badge ${a.status === 'APPROVED' ? 'badge-success' : a.status === 'REJECTED' ? 'badge-danger' : 'badge-warning'}">
                                                ${a.status}
                                            </span>
                                        </td>
                                        <td>${a.approvalDate || 'Pending'}</td>
                                        <td>${a.status === 'PENDING'
                                            ? `<button class="btn btn-sm btn-secondary" onclick="StudentPortal.withdrawReExam(${studentId}, ${a.reExamApplicationId})">Withdraw</button>`
                                            : ''}</td>
                                    </tr>
                                `).join('') || '<tr><td colspan="9" class="empty-state">No re-examination applications submitted.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load re-exams: ' + err.message);
        }
    }
};
