// Faculty Portal Module

const FacultyPortal = {
    async loadOfferings(facultyId) {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading assigned course offerings...');

        try {
            const offerings = await API.getFacultyOfferings(facultyId);

            let totalEnrolled = offerings.reduce((sum, o) => sum + (o.enrolledCount || 0), 0);

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Assigned Course Offerings</h2>
                        <p>Manage course sections, conduct classes, and assess student performance</p>
                    </div>
                </div>

                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-info">
                            <div class="label">Assigned Courses</div>
                            <div class="value">${offerings.length}</div>
                            <div class="subtitle">Active Offerings</div>
                        </div>
                        <div class="stat-icon blue">📖</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-info">
                            <div class="label">Total Students</div>
                            <div class="value">${totalEnrolled}</div>
                            <div class="subtitle">Across All Sections</div>
                        </div>
                        <div class="stat-icon green">👥</div>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>My Assigned Courses</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Offering ID</th>
                                    <th>Course Name</th>
                                    <th>Type</th>
                                    <th>Sem & Year</th>
                                    <th>Section & Room</th>
                                    <th>Classes Conducted</th>
                                    <th>Enrolled Students</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${offerings.map(o => `
                                    <tr>
                                        <td>#${o.offeringId}</td>
                                        <td><strong>${o.courseName}</strong> (${o.credits} cr)</td>
                                        <td><span class="badge badge-info">${o.courseType}</span></td>
                                        <td>Sem ${o.semester} &bull; ${o.academicYear}</td>
                                        <td>Sec ${o.section} (${o.roomNo || 'TBD'})</td>
                                        <td>
                                            <strong>${o.classesHeld}</strong> Classes Held
                                            <button class="btn btn-sm btn-secondary" style="margin-left: 8px;" onclick="FacultyPortal.openClassesHeldModal(${facultyId}, ${o.offeringId}, ${o.classesHeld}, '${o.courseName}')">Edit</button>
                                        </td>
                                        <td>
                                            <span class="badge badge-success">${o.enrolledCount} / ${o.maxSeats}</span>
                                        </td>
                                        <td>
                                            <button class="btn btn-sm btn-primary" onclick="FacultyPortal.loadStudents(${facultyId}, ${o.offeringId}, '${o.courseName}', '${o.section}')">
                                                Manage Students & Marks
                                            </button>
                                        </td>
                                    </tr>
                                `).join('') || '<tr><td colspan="8" class="empty-state">No courses currently assigned.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load offerings: ' + err.message);
        }
    },

    async loadStudents(facultyId, offeringId, courseName, section) {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner(`Loading enrolled students for ${courseName}...`);

        try {
            const students = await API.getEnrolledStudents(facultyId, offeringId);

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>${courseName} (Section ${section})</h2>
                        <p>Offering #${offeringId} &bull; Enrolled Students Roster, Attendance & Marks Entry</p>
                    </div>
                    <button class="btn btn-secondary" onclick="FacultyPortal.loadOfferings(${facultyId})">Back to Courses</button>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>Student Performance Roster (${students.length} Students)</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Roll No</th>
                                    <th>Student Name</th>
                                    <th>Program</th>
                                    <th>Attendance</th>
                                    <th>CA Marks (30)</th>
                                    <th>Midterm (30)</th>
                                    <th>Final (50)</th>
                                    <th>Total (100)</th>
                                    <th>Update Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${students.map(s => {
                                    const pct = s.attendancePercentage || 0;
                                    const isShortage = pct < 75;
                                    return `
                                        <tr>
                                            <td><strong>${s.studentId}</strong></td>
                                            <td>
                                                <div>${s.studentName}</div>
                                                <div style="font-size: 11px; color: var(--gray-600);">${s.email}</div>
                                            </td>
                                            <td>${s.programName}</td>
                                            <td>
                                                <div>${s.classesAttended} / ${s.classesHeld} (${pct}%)</div>
                                                ${isShortage ? '<span class="badge badge-danger">SHORTAGE</span>' : ''}
                                            </td>
                                            <td>${s.continuousAssessmentMarks}</td>
                                            <td>${s.midtermMarks}</td>
                                            <td>${s.finalExamMarks}</td>
                                            <td><strong>${s.totalMarks}</strong></td>
                                            <td>
                                                <div style="display: flex; gap: 6px;">
                                                    <button class="btn btn-sm btn-secondary" onclick="FacultyPortal.openAttendanceModal(${facultyId}, ${offeringId}, ${s.studentId}, '${s.studentName}', ${s.classesAttended}, ${s.classesHeld})">
                                                        Attendance
                                                    </button>
                                                    <button class="btn btn-sm btn-primary" onclick="FacultyPortal.openMarksModal(${facultyId}, ${offeringId}, ${s.studentId}, '${s.studentName}', ${s.continuousAssessmentMarks}, ${s.midtermMarks}, ${s.finalExamMarks})">
                                                        Marks
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    `;
                                }).join('') || '<tr><td colspan="9" class="empty-state">No students enrolled in this section yet.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load enrolled students: ' + err.message);
        }
    },

    openClassesHeldModal(facultyId, offeringId, currentHeld, courseName) {
        const modalHtml = `
            <div class="modal-backdrop" id="classes-held-modal">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3>Update Total Classes Held</h3>
                        <button class="modal-close" onclick="UI.closeModal('classes-held-modal')">&times;</button>
                    </div>
                    <form onsubmit="FacultyPortal.submitClassesHeld(event, ${facultyId}, ${offeringId})">
                        <div class="modal-body">
                            <p style="margin-bottom: 16px; color: var(--gray-600);">Course: <strong>${courseName}</strong></p>
                            <div class="form-group">
                                <label class="form-label">Total Classes Conducted to Date</label>
                                <input type="number" id="input-classes-held" class="form-control" min="0" max="200" value="${currentHeld}" required />
                                <small style="color: var(--gray-600);">Must be greater than or equal to student attendance records.</small>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" onclick="UI.closeModal('classes-held-modal')">Cancel</button>
                            <button type="submit" class="btn btn-primary">Save Classes Held</button>
                        </div>
                    </form>
                </div>
            </div>
        `;
        UI.openModal(modalHtml);
    },

    async submitClassesHeld(event, facultyId, offeringId) {
        event.preventDefault();
        const classesHeld = parseInt(document.getElementById('input-classes-held').value);

        try {
            await API.updateClassesHeld(facultyId, offeringId, classesHeld);
            UI.closeModal('classes-held-modal');
            UI.showToast('Classes held updated successfully!', 'success');
            FacultyPortal.loadOfferings(facultyId);
        } catch (err) {
            // Handled
        }
    },

    openAttendanceModal(facultyId, offeringId, studentId, studentName, currentAttended, classesHeld) {
        const modalHtml = `
            <div class="modal-backdrop" id="attendance-modal">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3>Enter Attendance Record</h3>
                        <button class="modal-close" onclick="UI.closeModal('attendance-modal')">&times;</button>
                    </div>
                    <form onsubmit="FacultyPortal.submitAttendance(event, ${facultyId}, ${offeringId}, ${studentId})">
                        <div class="modal-body">
                            <p style="margin-bottom: 16px; color: var(--gray-600);">
                                Student: <strong>${studentName}</strong> (Roll: ${studentId})<br/>
                                Total Classes Held: <strong>${classesHeld}</strong>
                            </p>
                            <div class="form-group">
                                <label class="form-label">Classes Attended</label>
                                <input type="number" id="input-classes-attended" class="form-control" min="0" max="${classesHeld}" value="${currentAttended}" required />
                                <small style="color: var(--gray-600);">Cannot exceed total classes held (${classesHeld}).</small>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" onclick="UI.closeModal('attendance-modal')">Cancel</button>
                            <button type="submit" class="btn btn-primary">Save Attendance</button>
                        </div>
                    </form>
                </div>
            </div>
        `;
        UI.openModal(modalHtml);
    },

    async submitAttendance(event, facultyId, offeringId, studentId) {
        event.preventDefault();
        const classesAttended = parseInt(document.getElementById('input-classes-attended').value);

        try {
            await API.updateAttendance(facultyId, {
                offeringId,
                studentId,
                classesAttended
            });
            UI.closeModal('attendance-modal');
            UI.showToast('Student attendance updated successfully!', 'success');
            FacultyPortal.loadOfferings(facultyId);
        } catch (err) {
            // Handled
        }
    },

    openMarksModal(facultyId, offeringId, studentId, studentName, ca, midterm, final) {
        const modalHtml = `
            <div class="modal-backdrop" id="marks-modal">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3>Enter Assessment Marks</h3>
                        <button class="modal-close" onclick="UI.closeModal('marks-modal')">&times;</button>
                    </div>
                    <form onsubmit="FacultyPortal.submitMarks(event, ${facultyId}, ${offeringId}, ${studentId})">
                        <div class="modal-body">
                            <p style="margin-bottom: 16px; color: var(--gray-600);">
                                Student: <strong>${studentName}</strong> (Roll: ${studentId})
                            </p>
                            <div class="form-group">
                                <label class="form-label">Continuous Assessment Marks (Max 30)</label>
                                <input type="number" step="0.5" id="input-ca" class="form-control" min="0" max="30" value="${ca}" required />
                            </div>
                            <div class="form-group">
                                <label class="form-label">Midterm Examination Marks (Max 30)</label>
                                <input type="number" step="0.5" id="input-mid" class="form-control" min="0" max="30" value="${midterm}" required />
                            </div>
                            <div class="form-group">
                                <label class="form-label">Final Examination Marks (Max 50)</label>
                                <input type="number" step="0.5" id="input-final" class="form-control" min="0" max="50" value="${final}" required />
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" onclick="UI.closeModal('marks-modal')">Cancel</button>
                            <button type="submit" class="btn btn-primary">Save Marks</button>
                        </div>
                    </form>
                </div>
            </div>
        `;
        UI.openModal(modalHtml);
    },

    async submitMarks(event, facultyId, offeringId, studentId) {
        event.preventDefault();
        const ca = parseFloat(document.getElementById('input-ca').value);
        const mid = parseFloat(document.getElementById('input-mid').value);
        const fin = parseFloat(document.getElementById('input-final').value);

        try {
            await API.updateMarks(facultyId, {
                offeringId,
                studentId,
                continuousAssessmentMarks: ca,
                midtermMarks: mid,
                finalExamMarks: fin
            });
            UI.closeModal('marks-modal');
            UI.showToast('Student marks updated successfully!', 'success');
            FacultyPortal.loadOfferings(facultyId);
        } catch (err) {
            // Handled
        }
    },

    async loadExams(facultyId) {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading faculty examination schedules...');

        try {
            const exams = await API.getFacultyExams(facultyId);

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Examination Schedules</h2>
                        <p>Scheduled assessments for your assigned course offerings</p>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>Scheduled Exams</h3>
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
                                    <th>Status</th>
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
                                        <td><span class="badge badge-success">${e.status}</span></td>
                                    </tr>
                                `).join('') || '<tr><td colspan="8" class="empty-state">No exams scheduled for your courses.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load exams: ' + err.message);
        }
    }
};
