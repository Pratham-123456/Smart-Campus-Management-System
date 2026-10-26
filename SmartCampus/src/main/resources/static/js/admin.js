// Admin Portal Module

const AdminPortal = {
    async loadDashboard() {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading administrative dashboard...');

        try {
            const stats = await API.getAdminStats();

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>University Academic Administration</h2>
                        <p>Institutional Overview &bull; Oracle Database 26ai Free (FREEPDB1)</p>
                    </div>
                    <div style="display: flex; gap: 8px;">
                        <button class="btn btn-primary" onclick="AdminPortal.openAddOfferingModal()">+ Create Offering</button>
                        <button class="btn btn-secondary" onclick="AdminPortal.openAddExamModal()">+ Schedule Exam</button>
                    </div>
                </div>

                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-info">
                            <div class="label">Total Students</div>
                            <div class="value">${stats.students}</div>
                            <div class="subtitle">Active Enrolled</div>
                        </div>
                        <div class="stat-icon blue">🎓</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-info">
                            <div class="label">Faculty Members</div>
                            <div class="value">${stats.faculty}</div>
                            <div class="subtitle">Professors & Instructors</div>
                        </div>
                        <div class="stat-icon green">👨‍🏫</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-info">
                            <div class="label">Course Catalog</div>
                            <div class="value">${stats.courses}</div>
                            <div class="subtitle">Curriculum Subjects</div>
                        </div>
                        <div class="stat-icon purple">📖</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-info">
                            <div class="label">Active Offerings</div>
                            <div class="value">${stats.offerings}</div>
                            <div class="subtitle">Semester Sections</div>
                        </div>
                        <div class="stat-icon orange">🏛️</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-info">
                            <div class="label">Total Enrollments</div>
                            <div class="value">${stats.enrollments}</div>
                            <div class="subtitle">Subject Registrations</div>
                        </div>
                        <div class="stat-icon green">📝</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-info">
                            <div class="label">Pending Re-Exams</div>
                            <div class="value">${stats.pendingReExams}</div>
                            <div class="subtitle">Awaiting Decision</div>
                        </div>
                        <div class="stat-icon ${stats.pendingReExams > 0 ? 'orange' : 'blue'}">⚖️</div>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>Quick Administration Shortcuts</h3>
                    </div>
                    <div class="card-body">
                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px;">
                            <button class="btn btn-secondary" style="padding: 14px; text-align: left; justify-content: flex-start;" onclick="App.navigateTo('admin-students')">
                                <div>
                                    <strong>Manage Students</strong>
                                    <div style="font-size: 11px; color: var(--gray-600);">Enroll and view profiles</div>
                                </div>
                            </button>
                            <button class="btn btn-secondary" style="padding: 14px; text-align: left; justify-content: flex-start;" onclick="App.navigateTo('admin-offerings')">
                                <div>
                                    <strong>Course Offerings</strong>
                                    <div style="font-size: 11px; color: var(--gray-600);">Assign faculty and rooms</div>
                                </div>
                            </button>
                            <button class="btn btn-secondary" style="padding: 14px; text-align: left; justify-content: flex-start;" onclick="App.navigateTo('admin-exams')">
                                <div>
                                    <strong>Examination Timetable</strong>
                                    <div style="font-size: 11px; color: var(--gray-600);">Schedule CAT and FAT exams</div>
                                </div>
                            </button>
                            <button class="btn btn-secondary" style="padding: 14px; text-align: left; justify-content: flex-start;" onclick="App.navigateTo('admin-reexams')">
                                <div>
                                    <strong>Re-Exam Governance</strong>
                                    <div style="font-size: 11px; color: var(--gray-600);">Approve/reject petitions</div>
                                </div>
                            </button>
                        </div>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load admin dashboard: ' + err.message);
        }
    },

    async loadDepartments() {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading departments...');

        try {
            const departments = await API.getDepartments();

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Departments Management</h2>
                        <p>Academic divisions and office locations</p>
                    </div>
                    <button class="btn btn-primary" onclick="AdminPortal.openAddDepartmentModal()">+ Add Department</button>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>All Departments (${departments.length})</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Department ID</th>
                                    <th>Department Name</th>
                                    <th>Office Location</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${departments.map(d => `
                                    <tr>
                                        <td>#${d.departmentId}</td>
                                        <td><strong>${d.departmentName}</strong></td>
                                        <td>${d.officeLocation || 'N/A'}</td>
                                    </tr>
                                `).join('') || '<tr><td colspan="3" class="empty-state">No departments registered.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load departments: ' + err.message);
        }
    },

    openAddDepartmentModal() {
        const modalHtml = `
            <div class="modal-backdrop" id="add-dept-modal">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3>Add New Department</h3>
                        <button class="modal-close" onclick="UI.closeModal('add-dept-modal')">&times;</button>
                    </div>
                    <form onsubmit="AdminPortal.submitDepartment(event)">
                        <div class="modal-body">
                            <div class="form-group">
                                <label class="form-label">Department Name</label>
                                <input type="text" id="dept-name" class="form-control" placeholder="e.g. Mechanical Engineering" required />
                            </div>
                            <div class="form-group">
                                <label class="form-label">Office Location</label>
                                <input type="text" id="dept-location" class="form-control" placeholder="e.g. MB-201" />
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" onclick="UI.closeModal('add-dept-modal')">Cancel</button>
                            <button type="submit" class="btn btn-primary">Create Department</button>
                        </div>
                    </form>
                </div>
            </div>
        `;
        UI.openModal(modalHtml);
    },

    async submitDepartment(event) {
        event.preventDefault();
        const departmentName = document.getElementById('dept-name').value;
        const officeLocation = document.getElementById('dept-location').value;

        try {
            await API.createDepartment({ departmentName, officeLocation });
            UI.closeModal('add-dept-modal');
            UI.showToast('Department created successfully!', 'success');
            AdminPortal.loadDepartments();
        } catch (err) {
            // Handled
        }
    },

    async loadPrograms() {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading degree programs...');

        try {
            const programs = await API.getPrograms();

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Degree Programs Management</h2>
                        <p>Academic degree curriculums and duration</p>
                    </div>
                    <button class="btn btn-primary" onclick="AdminPortal.openAddProgramModal()">+ Add Program</button>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>All Degree Programs (${programs.length})</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Program ID</th>
                                    <th>Program Name</th>
                                    <th>Degree</th>
                                    <th>Duration</th>
                                    <th>Department</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${programs.map(p => `
                                    <tr>
                                        <td>#${p.programId}</td>
                                        <td><strong>${p.programName}</strong></td>
                                        <td><span class="badge badge-info">${p.degree}</span></td>
                                        <td>${p.durationYears} Years</td>
                                        <td>${p.departmentName}</td>
                                    </tr>
                                `).join('') || '<tr><td colspan="5" class="empty-state">No programs configured.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load programs: ' + err.message);
        }
    },

    async openAddProgramModal() {
        try {
            const depts = await API.getDepartments();
            const modalHtml = `
                <div class="modal-backdrop" id="add-prog-modal">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h3>Add Degree Program</h3>
                            <button class="modal-close" onclick="UI.closeModal('add-prog-modal')">&times;</button>
                        </div>
                        <form onsubmit="AdminPortal.submitProgram(event)">
                            <div class="modal-body">
                                <div class="form-group">
                                    <label class="form-label">Program Name</label>
                                    <input type="text" id="prog-name" class="form-control" placeholder="e.g. Information Technology" required />
                                </div>
                                <div class="form-grid-2">
                                    <div class="form-group">
                                        <label class="form-label">Degree Level</label>
                                        <input type="text" id="prog-degree" class="form-control" placeholder="e.g. B.Tech" required />
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Duration (Years)</label>
                                        <input type="number" id="prog-duration" class="form-control" min="1" max="6" value="4" required />
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Department</label>
                                    <select id="prog-dept" class="form-control" required>
                                        ${depts.map(d => `<option value="${d.departmentId}">${d.departmentName}</option>`).join('')}
                                    </select>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" onclick="UI.closeModal('add-prog-modal')">Cancel</button>
                                <button type="submit" class="btn btn-primary">Create Program</button>
                            </div>
                        </form>
                    </div>
                </div>
            `;
            UI.openModal(modalHtml);
        } catch (err) {
            UI.showToast('Could not load departments for program creation: ' + err.message, 'error');
        }
    },

    async submitProgram(event) {
        event.preventDefault();
        const programName = document.getElementById('prog-name').value;
        const degree = document.getElementById('prog-degree').value;
        const durationYears = parseInt(document.getElementById('prog-duration').value);
        const departmentId = parseInt(document.getElementById('prog-dept').value);

        try {
            await API.createProgram({ programName, degree, durationYears, departmentId });
            UI.closeModal('add-prog-modal');
            UI.showToast('Program created successfully!', 'success');
            AdminPortal.loadPrograms();
        } catch (err) {
            // Handled
        }
    },

    async loadStudents() {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading student directory...');

        try {
            const students = await API.getStudents();

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Student Directory</h2>
                        <p>Registered university students across all degree programs</p>
                    </div>
                    <button class="btn btn-primary" onclick="AdminPortal.openAddStudentModal()">+ Add Student</button>
                </div>

                <div class="card">
                    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                        <h3>Registered Students (${students.length})</h3>
                        <input type="text" class="form-input" placeholder="🔍 Search students by name, ID, or program..." style="max-width: 320px; height: 34px; font-size: 13px;" oninput="UI.filterTable('#admin-students-table', this.value)" />
                    </div>
                    <div class="table-responsive">
                        <table class="data-table" id="admin-students-table">
                            <thead>
                                <tr>
                                    <th>Roll No</th>
                                    <th>Name</th>
                                    <th>Email</th>
                                    <th>Phone</th>
                                    <th>Program</th>
                                    <th>Admission Year</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${students.map(s => `
                                    <tr>
                                        <td><strong>#${s.studentId}</strong></td>
                                        <td>${s.name}</td>
                                        <td>${s.email}</td>
                                        <td>${s.phone || 'N/A'}</td>
                                        <td>${s.degree} - ${s.programName}</td>
                                        <td>${s.admissionYear}</td>
                                    </tr>
                                `).join('') || '<tr><td colspan="6" class="empty-state">No students found.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load students: ' + err.message);
        }
    },

    async openAddStudentModal() {
        try {
            const programs = await API.getPrograms();
            const modalHtml = `
                <div class="modal-backdrop" id="add-student-modal">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h3>Add New Student</h3>
                            <button class="modal-close" onclick="UI.closeModal('add-student-modal')">&times;</button>
                        </div>
                        <form onsubmit="AdminPortal.submitStudent(event)">
                            <div class="modal-body">
                                <div class="form-group">
                                    <label class="form-label">Full Name</label>
                                    <input type="text" id="stu-name" class="form-control" placeholder="e.g. Diya Sen" required />
                                </div>
                                <div class="form-grid-2">
                                    <div class="form-group">
                                        <label class="form-label">College Email</label>
                                        <input type="email" id="stu-email" class="form-control" placeholder="e.g. diya.sen@example.com" required />
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Phone Number</label>
                                        <input type="text" id="stu-phone" class="form-control" placeholder="9876500004" />
                                    </div>
                                </div>
                                <div class="form-grid-2">
                                    <div class="form-group">
                                        <label class="form-label">Date of Birth</label>
                                        <input type="date" id="stu-dob" class="form-control" />
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Admission Year</label>
                                        <input type="number" id="stu-year" class="form-control" value="2026" min="2000" max="2100" required />
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Academic Program</label>
                                    <select id="stu-prog" class="form-control" required>
                                        ${programs.map(p => `<option value="${p.programId}">${p.degree} - ${p.programName}</option>`).join('')}
                                    </select>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" onclick="UI.closeModal('add-student-modal')">Cancel</button>
                                <button type="submit" class="btn btn-primary">Enroll Student</button>
                            </div>
                        </form>
                    </div>
                </div>
            `;
            UI.openModal(modalHtml);
        } catch (err) {
            UI.showToast('Could not load programs: ' + err.message, 'error');
        }
    },

    async submitStudent(event) {
        event.preventDefault();
        const name = document.getElementById('stu-name').value;
        const email = document.getElementById('stu-email').value;
        const phone = document.getElementById('stu-phone').value;
        const dob = document.getElementById('stu-dob').value || null;
        const admissionYear = parseInt(document.getElementById('stu-year').value);
        const programId = parseInt(document.getElementById('stu-prog').value);

        try {
            await API.createStudent({ name, email, phone, dob, admissionYear, programId });
            UI.closeModal('add-student-modal');
            UI.showToast('Student enrolled successfully!', 'success');
            AdminPortal.loadStudents();
        } catch (err) {
            // Handled
        }
    },

    async loadFaculty() {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading faculty members...');

        try {
            const faculty = await API.getFaculty();

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Faculty Management</h2>
                        <p>Academic professors and instructors</p>
                    </div>
                    <button class="btn btn-primary" onclick="AdminPortal.openAddFacultyModal()">+ Add Faculty</button>
                </div>

                <div class="card">
                    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                        <h3>Faculty Members (${faculty.length})</h3>
                        <input type="text" class="form-input" placeholder="🔍 Search faculty by name, department, or title..." style="max-width: 320px; height: 34px; font-size: 13px;" oninput="UI.filterTable('#admin-faculty-table', this.value)" />
                    </div>
                    <div class="table-responsive">
                        <table class="data-table" id="admin-faculty-table">
                            <thead>
                                <tr>
                                    <th>Faculty ID</th>
                                    <th>Name</th>
                                    <th>Email</th>
                                    <th>Phone</th>
                                    <th>Designation</th>
                                    <th>Department</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${faculty.map(f => `
                                    <tr>
                                        <td>#${f.facultyId}</td>
                                        <td><strong>${f.name}</strong></td>
                                        <td>${f.email}</td>
                                        <td>${f.phone || 'N/A'}</td>
                                        <td><span class="badge badge-info">${f.designation}</span></td>
                                        <td>${f.departmentName}</td>
                                    </tr>
                                `).join('') || '<tr><td colspan="6" class="empty-state">No faculty members found.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load faculty: ' + err.message);
        }
    },

    async openAddFacultyModal() {
        try {
            const depts = await API.getDepartments();
            const modalHtml = `
                <div class="modal-backdrop" id="add-fac-modal">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h3>Add Faculty Member</h3>
                            <button class="modal-close" onclick="UI.closeModal('add-fac-modal')">&times;</button>
                        </div>
                        <form onsubmit="AdminPortal.submitFaculty(event)">
                            <div class="modal-body">
                                <div class="form-group">
                                    <label class="form-label">Full Name</label>
                                    <input type="text" id="fac-name" class="form-control" placeholder="e.g. Dr. Rajesh Kumar" required />
                                </div>
                                <div class="form-grid-2">
                                    <div class="form-group">
                                        <label class="form-label">Email</label>
                                        <input type="email" id="fac-email" class="form-control" placeholder="rajesh.kumar@example.com" required />
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Phone</label>
                                        <input type="text" id="fac-phone" class="form-control" placeholder="9876510003" />
                                    </div>
                                </div>
                                <div class="form-grid-2">
                                    <div class="form-group">
                                        <label class="form-label">Designation</label>
                                        <input type="text" id="fac-desig" class="form-control" placeholder="e.g. Associate Professor" required />
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Department</label>
                                        <select id="fac-dept" class="form-control" required>
                                            ${depts.map(d => `<option value="${d.departmentId}">${d.departmentName}</option>`).join('')}
                                        </select>
                                    </div>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" onclick="UI.closeModal('add-fac-modal')">Cancel</button>
                                <button type="submit" class="btn btn-primary">Save Faculty</button>
                            </div>
                        </form>
                    </div>
                </div>
            `;
            UI.openModal(modalHtml);
        } catch (err) {
            UI.showToast('Could not load departments: ' + err.message, 'error');
        }
    },

    async submitFaculty(event) {
        event.preventDefault();
        const name = document.getElementById('fac-name').value;
        const email = document.getElementById('fac-email').value;
        const phone = document.getElementById('fac-phone').value;
        const designation = document.getElementById('fac-desig').value;
        const departmentId = parseInt(document.getElementById('fac-dept').value);

        try {
            await API.createFaculty({ name, email, phone, designation, departmentId });
            UI.closeModal('add-fac-modal');
            UI.showToast('Faculty member added successfully!', 'success');
            AdminPortal.loadFaculty();
        } catch (err) {
            // Handled
        }
    },

    async loadCourses() {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading courses catalog...');

        try {
            const courses = await API.getCourses();

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Course Catalog</h2>
                        <p>Master curriculum courses and credits</p>
                    </div>
                    <button class="btn btn-primary" onclick="AdminPortal.openAddCourseModal()">+ Add Course</button>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>All Curriculum Courses (${courses.length})</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Course ID</th>
                                    <th>Course Name</th>
                                    <th>Credits</th>
                                    <th>Course Type</th>
                                    <th>Department</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${courses.map(c => `
                                    <tr>
                                        <td>#${c.courseId}</td>
                                        <td><strong>${c.courseName}</strong></td>
                                        <td>${c.credits} Credits</td>
                                        <td><span class="badge badge-info">${c.courseType}</span></td>
                                        <td>${c.departmentName}</td>
                                    </tr>
                                `).join('') || '<tr><td colspan="5" class="empty-state">No courses registered.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load courses: ' + err.message);
        }
    },

    async openAddCourseModal() {
        try {
            const depts = await API.getDepartments();
            const modalHtml = `
                <div class="modal-backdrop" id="add-course-modal">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h3>Add Master Course</h3>
                            <button class="modal-close" onclick="UI.closeModal('add-course-modal')">&times;</button>
                        </div>
                        <form onsubmit="AdminPortal.submitCourse(event)">
                            <div class="modal-body">
                                <div class="form-group">
                                    <label class="form-label">Course Title</label>
                                    <input type="text" id="course-name" class="form-control" placeholder="e.g. Operating Systems" required />
                                </div>
                                <div class="form-grid-2">
                                    <div class="form-group">
                                        <label class="form-label">Credits</label>
                                        <input type="number" id="course-credits" class="form-control" min="1" max="10" value="4" required />
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Course Type</label>
                                        <select id="course-type" class="form-control" required>
                                            <option value="Core">Core</option>
                                            <option value="Elective">Elective</option>
                                            <option value="Lab">Lab</option>
                                        </select>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Department</label>
                                    <select id="course-dept" class="form-control" required>
                                        ${depts.map(d => `<option value="${d.departmentId}">${d.departmentName}</option>`).join('')}
                                    </select>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" onclick="UI.closeModal('add-course-modal')">Cancel</button>
                                <button type="submit" class="btn btn-primary">Create Course</button>
                            </div>
                        </form>
                    </div>
                </div>
            `;
            UI.openModal(modalHtml);
        } catch (err) {
            UI.showToast('Could not load departments: ' + err.message, 'error');
        }
    },

    async submitCourse(event) {
        event.preventDefault();
        const courseName = document.getElementById('course-name').value;
        const credits = parseInt(document.getElementById('course-credits').value);
        const courseType = document.getElementById('course-type').value;
        const departmentId = parseInt(document.getElementById('course-dept').value);

        try {
            await API.createCourse({ courseName, credits, courseType, departmentId });
            UI.closeModal('add-course-modal');
            UI.showToast('Course created successfully!', 'success');
            AdminPortal.loadCourses();
        } catch (err) {
            // Handled
        }
    },

    async loadOfferings() {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading semester course offerings...');

        try {
            const offerings = await API.getOfferings();

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Semester Course Offerings</h2>
                        <p>Manage faculty assignments, sections, room numbers, and seat capacities</p>
                    </div>
                    <button class="btn btn-primary" onclick="AdminPortal.openAddOfferingModal()">+ Create Course Offering</button>
                </div>

                <div class="card">
                    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                        <h3>Current Offerings (${offerings.length})</h3>
                        <input type="text" class="form-input" placeholder="🔍 Search offerings by course, faculty, or room..." style="max-width: 320px; height: 34px; font-size: 13px;" oninput="UI.filterTable('#admin-offerings-table', this.value)" />
                    </div>
                    <div class="table-responsive">
                        <table class="data-table" id="admin-offerings-table">
                            <thead>
                                <tr>
                                    <th>Offering ID</th>
                                    <th>Course</th>
                                    <th>Faculty</th>
                                    <th>Sem & Year</th>
                                    <th>Sec & Room</th>
                                    <th>Max Seats</th>
                                    <th>Enrolled</th>
                                    <th>Classes Held</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${offerings.map(o => `
                                    <tr>
                                        <td>#${o.offeringId}</td>
                                        <td><strong>${o.courseName}</strong></td>
                                        <td>${o.facultyName}</td>
                                        <td>Sem ${o.semester} &bull; ${o.academicYear}</td>
                                        <td>Sec ${o.section} (${o.roomNo || 'TBD'})</td>
                                        <td>${o.maxSeats}</td>
                                        <td><span class="badge badge-success">${o.enrolledCount} Enrolled</span></td>
                                        <td>${o.classesHeld}</td>
                                    </tr>
                                `).join('') || '<tr><td colspan="8" class="empty-state">No course offerings created yet.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load offerings: ' + err.message);
        }
    },

    async openAddOfferingModal() {
        try {
            const [courses, faculty] = await Promise.all([API.getCourses(), API.getFaculty()]);
            const modalHtml = `
                <div class="modal-backdrop" id="add-off-modal">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h3>Create Course Offering</h3>
                            <button class="modal-close" onclick="UI.closeModal('add-off-modal')">&times;</button>
                        </div>
                        <form onsubmit="AdminPortal.submitOffering(event)">
                            <div class="modal-body">
                                <div class="form-group">
                                    <label class="form-label">Course</label>
                                    <select id="off-course" class="form-control" required>
                                        ${courses.map(c => `<option value="${c.courseId}">${c.courseName} (${c.credits} cr)</option>`).join('')}
                                    </select>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Faculty Instructor</label>
                                    <select id="off-faculty" class="form-control" required>
                                        ${faculty.map(f => `<option value="${f.facultyId}">${f.name} (${f.designation})</option>`).join('')}
                                    </select>
                                </div>
                                <div class="form-grid-2">
                                    <div class="form-group">
                                        <label class="form-label">Semester (1-10)</label>
                                        <input type="number" id="off-sem" class="form-control" min="1" max="10" value="3" required />
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Academic Year</label>
                                        <input type="text" id="off-year" class="form-control" placeholder="2026-2027" value="2026-2027" required />
                                    </div>
                                </div>
                                <div class="form-grid-2">
                                    <div class="form-group">
                                        <label class="form-label">Section</label>
                                        <input type="text" id="off-sec" class="form-control" placeholder="A" value="A" required />
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Room Number</label>
                                        <input type="text" id="off-room" class="form-control" placeholder="SJT-101" />
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label class="form-label">Maximum Seat Capacity</label>
                                    <input type="number" id="off-seats" class="form-control" min="1" max="300" value="60" required />
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" onclick="UI.closeModal('add-off-modal')">Cancel</button>
                                <button type="submit" class="btn btn-primary">Create Offering</button>
                            </div>
                        </form>
                    </div>
                </div>
            `;
            UI.openModal(modalHtml);
        } catch (err) {
            UI.showToast('Could not load course or faculty options: ' + err.message, 'error');
        }
    },

    async submitOffering(event) {
        event.preventDefault();
        const courseId = parseInt(document.getElementById('off-course').value);
        const facultyId = parseInt(document.getElementById('off-faculty').value);
        const semester = parseInt(document.getElementById('off-sem').value);
        const academicYear = document.getElementById('off-year').value;
        const section = document.getElementById('off-sec').value.trim();
        const roomNo = document.getElementById('off-room').value.trim();
        const maxSeats = parseInt(document.getElementById('off-seats').value);

        try {
            await API.createOffering({ courseId, facultyId, semester, academicYear, section, roomNo, maxSeats });
            UI.closeModal('add-off-modal');
            UI.showToast('Course offering created successfully!', 'success');
            AdminPortal.loadOfferings();
        } catch (err) {
            // Handled
        }
    },

    async loadExams() {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading examination schedules...');

        try {
            const exams = await API.getAdminExams();

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Examination Timetable & Schedules</h2>
                        <p>Configure examination dates, timings, venues, and types</p>
                    </div>
                    <button class="btn btn-primary" onclick="AdminPortal.openAddExamModal()">+ Schedule Examination</button>
                </div>

                <div class="card">
                    <div class="card-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                        <h3>Scheduled Examinations (${exams.length})</h3>
                        <input type="text" class="form-input" placeholder="🔍 Search exams by course, faculty, or type..." style="max-width: 320px; height: 34px; font-size: 13px;" oninput="UI.filterTable('#admin-exams-table', this.value)" />
                    </div>
                    <div class="table-responsive">
                        <table class="data-table" id="admin-exams-table">
                            <thead>
                                <tr>
                                    <th>Exam ID</th>
                                    <th>Course</th>
                                    <th>Faculty</th>
                                    <th>Type</th>
                                    <th>Exam Date</th>
                                    <th>Timing</th>
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
                                        <td>${e.facultyName}</td>
                                        <td><span class="badge badge-info">${e.examType}</span></td>
                                        <td>${e.examDate}</td>
                                        <td>${e.startTime ? e.startTime.replace('T', ' ') : ''} - ${e.endTime ? e.endTime.split('T')[1] : ''}</td>
                                        <td>${e.venue}</td>
                                        <td>${e.maxMarks}</td>
                                        <td><span class="badge badge-success">${e.status}</span></td>
                                    </tr>
                                `).join('') || '<tr><td colspan="9" class="empty-state">No exams scheduled yet.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load exams: ' + err.message);
        }
    },

    async openAddExamModal() {
        try {
            const offerings = await API.getOfferings();
            const modalHtml = `
                <div class="modal-backdrop" id="add-exam-modal">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h3>Schedule New Examination</h3>
                            <button class="modal-close" onclick="UI.closeModal('add-exam-modal')">&times;</button>
                        </div>
                        <form onsubmit="AdminPortal.submitExam(event)">
                            <div class="modal-body">
                                <div class="form-group">
                                    <label class="form-label">Course Offering</label>
                                    <select id="exam-offering" class="form-control" required>
                                        ${offerings.map(o => `<option value="${o.offeringId}">${o.courseName} (Sec ${o.section}) - ${o.facultyName}</option>`).join('')}
                                    </select>
                                </div>
                                <div class="form-grid-2">
                                    <div class="form-group">
                                        <label class="form-label">Exam Type</label>
                                        <select id="exam-type" class="form-control" required>
                                            <option value="CAT1">CAT1</option>
                                            <option value="CAT2">CAT2</option>
                                            <option value="FAT">FAT (Final Assessment)</option>
                                            <option value="LAB">Lab Exam</option>
                                        </select>
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Exam Date</label>
                                        <input type="date" id="exam-date" class="form-control" value="2026-11-15" required />
                                    </div>
                                </div>
                                <div class="form-grid-2">
                                    <div class="form-group">
                                        <label class="form-label">Start Time</label>
                                        <input type="text" id="exam-start" class="form-control" placeholder="2026-11-15 09:00:00" value="2026-11-15 09:00:00" required />
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">End Time</label>
                                        <input type="text" id="exam-end" class="form-control" placeholder="2026-11-15 10:30:00" value="2026-11-15 10:30:00" required />
                                    </div>
                                </div>
                                <div class="form-grid-2">
                                    <div class="form-group">
                                        <label class="form-label">Venue / Hall</label>
                                        <input type="text" id="exam-venue" class="form-control" placeholder="SJT-301" value="SJT-301" required />
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label">Maximum Marks</label>
                                        <input type="number" id="exam-max" class="form-control" min="1" max="100" value="50" required />
                                    </div>
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn btn-secondary" onclick="UI.closeModal('add-exam-modal')">Cancel</button>
                                <button type="submit" class="btn btn-primary">Publish Schedule</button>
                            </div>
                        </form>
                    </div>
                </div>
            `;
            UI.openModal(modalHtml);
        } catch (err) {
            UI.showToast('Could not load course offerings: ' + err.message, 'error');
        }
    },

    async submitExam(event) {
        event.preventDefault();
        const offeringId = parseInt(document.getElementById('exam-offering').value);
        const examType = document.getElementById('exam-type').value;
        const examDate = document.getElementById('exam-date').value;
        const startTime = document.getElementById('exam-start').value;
        const endTime = document.getElementById('exam-end').value;
        const venue = document.getElementById('exam-venue').value;
        const maxMarks = parseFloat(document.getElementById('exam-max').value);

        try {
            await API.createExam({ offeringId, examType, examDate, startTime, endTime, venue, maxMarks });
            UI.closeModal('add-exam-modal');
            UI.showToast('Exam schedule created successfully!', 'success');
            AdminPortal.loadExams();
        } catch (err) {
            // Handled
        }
    },

    async loadResults() {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading all exam results...');

        try {
            const results = await API.getAdminResults();

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>University Exam Results Registry</h2>
                        <p>Complete published result sheets across all courses</p>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>Published Results (${results.length})</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Result ID</th>
                                    <th>Student</th>
                                    <th>Course</th>
                                    <th>Exam</th>
                                    <th>Marks Obtained</th>
                                    <th>Status</th>
                                    <th>Grade</th>
                                    <th>Published Date</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${results.map(r => `
                                    <tr>
                                        <td>#${r.examResultId}</td>
                                        <td><strong>${r.studentName}</strong> (Roll: ${r.studentId})</td>
                                        <td>${r.courseName}</td>
                                        <td><span class="badge badge-info">${r.examType}</span></td>
                                        <td><strong>${r.marksObtained}</strong> / ${r.maxMarks}</td>
                                        <td><span class="badge ${r.resultStatus === 'PASS' ? 'badge-success' : 'badge-danger'}">${r.resultStatus}</span></td>
                                        <td><strong>Grade ${r.gradeCode || 'N/A'}</strong></td>
                                        <td>${r.publishedDate || 'N/A'}</td>
                                    </tr>
                                `).join('') || '<tr><td colspan="8" class="empty-state">No results published yet.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load results: ' + err.message);
        }
    },

    async loadReExams() {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading re-exam applications...');

        try {
            const applications = await API.getAdminReExams();

            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>Re-Examination Applications Governance</h2>
                        <p>Review and decide student re-examination petitions</p>
                    </div>
                </div>

                <div class="card">
                    <div class="card-header">
                        <h3>Applications Inbox (${applications.length})</h3>
                    </div>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>App ID</th>
                                    <th>Student</th>
                                    <th>Course & Exam</th>
                                    <th>Score</th>
                                    <th>Reason Submitted</th>
                                    <th>Date</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${applications.map(a => `
                                    <tr>
                                        <td>#${a.reExamApplicationId}</td>
                                        <td><strong>${a.studentName}</strong> (Roll: ${a.studentId})</td>
                                        <td>${a.courseName} &bull; ${a.examType}</td>
                                        <td>${a.marksObtained} (Grade ${a.gradeCode})</td>
                                        <td><em>"${a.reason}"</em></td>
                                        <td>${a.applicationDate}</td>
                                        <td>
                                            <span class="badge ${a.status === 'APPROVED' ? 'badge-success' : a.status === 'REJECTED' ? 'badge-danger' : 'badge-warning'}">
                                                ${a.status}
                                            </span>
                                        </td>
                                        <td>
                                            ${a.status === 'PENDING' ? `
                                                <div style="display: flex; gap: 6px;">
                                                    <button class="btn btn-sm btn-success" onclick="AdminPortal.decideReExam(${a.reExamApplicationId}, 'APPROVED')">Approve</button>
                                                    <button class="btn btn-sm btn-danger" onclick="AdminPortal.decideReExam(${a.reExamApplicationId}, 'REJECTED')">Reject</button>
                                                </div>
                                            ` : `<small style="color: var(--gray-600);">${a.approvalDate || 'Decided'}</small>`}
                                        </td>
                                    </tr>
                                `).join('') || '<tr><td colspan="8" class="empty-state">No re-examination applications in queue.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load re-exam applications: ' + err.message);
        }
    },

    async decideReExam(applicationId, decision) {
        if (!confirm(`Are you sure you want to mark Application #${applicationId} as ${decision}?`)) {
            return;
        }

        try {
            await API.decideReExam(applicationId, decision);
            UI.showToast(`Application #${applicationId} marked as ${decision}!`, 'success');
            AdminPortal.loadReExams();
        } catch (err) {
            // Handled
        }
    },

    async loadProvisioningConsole() {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading accounts and directories...');

        try {
            const [students, faculty] = await Promise.all([
                API.getAdminStudents(),
                API.getAdminFaculty()
            ]);

            container.innerHTML = `
                <div class="dashboard-header">
                    <div class="dashboard-title">
                        <h1>User Account Provisioning & Access Control</h1>
                        <p>Provision genuine credentials and BCrypt-hashed passwords for university members &bull; XYZ University</p>
                    </div>
                </div>

                <!-- Students Directory & Provisioning Card -->
                <div class="data-card">
                    <div class="data-card-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                        <div class="data-card-title">🎓 Student Accounts Provisioning (${students.length} Enrolled Students)</div>
                        <input type="text" class="form-input" placeholder="🔍 Search students..." style="max-width: 280px; height: 34px; font-size: 13px;" oninput="UI.filterTable('#prov-students-table', this.value)" />
                    </div>
                    <div class="table-wrapper">
                        <table class="academic-table" id="prov-students-table">
                            <thead>
                                <tr>
                                    <th>Student ID</th>
                                    <th>Student Name</th>
                                    <th>Email</th>
                                    <th>Degree & Program</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${students.map(s => `
                                    <tr>
                                        <td><strong>#${s.studentId}</strong></td>
                                        <td>${s.name}</td>
                                        <td>${s.email}</td>
                                        <td>${s.degree} &bull; ${s.programName}</td>
                                        <td>
                                            <button class="btn btn-secondary btn-sm" onclick="AdminPortal.openProvisionModal('STUDENT', ${s.studentId}, '${s.name}', '${s.email}')">
                                                🔑 Provision / Reset Credentials
                                            </button>
                                        </td>
                                    </tr>
                                `).join('') || '<tr><td colspan="5" class="empty-state">No students found.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Faculty Directory & Provisioning Card -->
                <div class="data-card">
                    <div class="data-card-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                        <div class="data-card-title">👨‍🏫 Faculty Accounts Provisioning (${faculty.length} Appointed Faculty)</div>
                        <input type="text" class="form-input" placeholder="🔍 Search faculty..." style="max-width: 280px; height: 34px; font-size: 13px;" oninput="UI.filterTable('#prov-faculty-table', this.value)" />
                    </div>
                    <div class="table-wrapper">
                        <table class="academic-table" id="prov-faculty-table">
                            <thead>
                                <tr>
                                    <th>Faculty ID</th>
                                    <th>Faculty Name</th>
                                    <th>Email</th>
                                    <th>Designation & Department</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${faculty.map(f => `
                                    <tr>
                                        <td><strong>#${f.facultyId}</strong></td>
                                        <td>${f.name}</td>
                                        <td>${f.email}</td>
                                        <td>${f.designation} &bull; ${f.departmentName}</td>
                                        <td>
                                            <button class="btn btn-secondary btn-sm" onclick="AdminPortal.openProvisionModal('FACULTY', ${f.facultyId}, '${f.name}', '${f.email}')">
                                                🔑 Provision / Reset Credentials
                                            </button>
                                        </td>
                                    </tr>
                                `).join('') || '<tr><td colspan="5" class="empty-state">No faculty found.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>
            `;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load user provisioning directory: ' + err.message);
        }
    },

    openProvisionModal(role, recordId, name, email) {
        const modalHtml = `
            <div class="modal-overlay" id="modal-provision-account">
                <div class="modal-dialog">
                    <div class="modal-header">
                        <h3>Provision Account: ${name}</h3>
                        <button class="modal-close-btn" onclick="UI.closeModal('modal-provision-account')">&times;</button>
                    </div>
                    <form onsubmit="AdminPortal.submitProvisionForm(event)">
                        <div class="modal-body">
                            <input type="hidden" id="prov-role" value="${role}">
                            <input type="hidden" id="prov-record-id" value="${recordId}">

                            <div class="login-alert info" style="margin-bottom: 16px;">
                                Setting credentials for <strong>${name}</strong> (${role} ID: #${recordId}).
                                Password will be hashed using BCrypt before storing in Oracle DB.
                            </div>

                            <div class="form-group">
                                <label class="form-label">Username / Login Identifier</label>
                                <input type="text" id="prov-username" class="form-input" value="${recordId}" required>
                                <small style="color: var(--color-text-muted); font-size: 11px;">e.g. Roll number or alphanumeric identifier (e.g. ${recordId})</small>
                            </div>

                            <div class="form-group">
                                <label class="form-label">Initial Password</label>
                                <input type="password" id="prov-password" class="form-input" placeholder="Min 6 characters" minlength="6" required>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" onclick="UI.closeModal('modal-provision-account')">Cancel</button>
                            <button type="submit" id="btn-submit-prov" class="btn btn-primary">Save & Provision Account</button>
                        </div>
                    </form>
                </div>
            </div>
        `;
        UI.openModal(modalHtml);
    },

    async submitProvisionForm(event) {
        event.preventDefault();
        const role = document.getElementById('prov-role').value;
        const recordId = parseInt(document.getElementById('prov-record-id').value);
        const username = document.getElementById('prov-username').value.trim();
        const password = document.getElementById('prov-password').value;
        const submitBtn = document.getElementById('btn-submit-prov');

        submitBtn.disabled = true;
        submitBtn.textContent = 'Provisioning...';

        try {
            await API.provisionAccount({ role, recordId, username, password });
            UI.closeModal('modal-provision-account');
            UI.showToast(`Account provisioned successfully for ${username}!`, 'success');
        } catch (err) {
            UI.showToast(err.message || 'Provisioning failed', 'error');
            submitBtn.disabled = false;
            submitBtn.textContent = 'Save & Provision Account';
        }
    }
};

