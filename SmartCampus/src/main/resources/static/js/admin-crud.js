// Phase 3: Update / Delete actions for the Administrator portal.
// Each admin table stores its rows in AdminCrud.cache[entity]; the Edit modal is
// generated from the field schema below and submits PUT /api/admin/{entity}/{id}.
// Deletes call DELETE /api/admin/{entity}/{id}; the server answers 409 while child
// rows exist, and the toast shows that message.

const AdminCrud = {
    cache: {},

    esc(value) {
        return String(value ?? '').replace(/[&<>"']/g, ch => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        }[ch]));
    },

    // Option loaders for foreign-key <select> fields
    options: {
        async departments() {
            return (await API.getDepartments()).map(d => [d.departmentId, `${d.departmentName} (#${d.departmentId})`]);
        },
        async programs() {
            return (await API.getPrograms()).map(p => [p.programId, `${p.programName} - ${p.degree} (#${p.programId})`]);
        },
        async courses() {
            return (await API.getCourses()).map(c => [c.courseId, `${c.courseName} (#${c.courseId})`]);
        },
        async faculty() {
            return (await API.getFaculty()).map(f => [f.facultyId, `${f.name} (#${f.facultyId})`]);
        },
        async offerings() {
            return (await API.getOfferings()).map(o => [o.offeringId, `#${o.offeringId} ${o.courseName} - Sec ${o.section} (Sem ${o.semester}, ${o.academicYear})`]);
        }
    },

    // key = JSON property; same names as the create DTOs and the GET payloads
    schemas: {
        departments: {
            title: 'Department', idKey: 'departmentId', reload: () => AdminPortal.loadDepartments(),
            fields: [
                { key: 'departmentName', label: 'Department Name', required: true },
                { key: 'officeLocation', label: 'Office Location' }
            ]
        },
        programs: {
            title: 'Program', idKey: 'programId', reload: () => AdminPortal.loadPrograms(),
            fields: [
                { key: 'programName', label: 'Program Name', required: true },
                { key: 'degree', label: 'Degree', required: true },
                { key: 'durationYears', label: 'Duration (years)', type: 'number', min: 1, max: 6, required: true },
                { key: 'departmentId', label: 'Department', type: 'select', source: 'departments', required: true }
            ]
        },
        students: {
            title: 'Student', idKey: 'studentId', reload: () => AdminPortal.loadStudents(),
            fields: [
                { key: 'name', label: 'Full Name', required: true },
                { key: 'email', label: 'Email', type: 'email', required: true },
                { key: 'phone', label: 'Phone' },
                { key: 'dob', label: 'Date of Birth', type: 'date' },
                { key: 'admissionYear', label: 'Admission Year', type: 'number', min: 2000, max: 2100, required: true },
                { key: 'programId', label: 'Program', type: 'select', source: 'programs', required: true }
            ]
        },
        faculty: {
            title: 'Faculty Member', idKey: 'facultyId', reload: () => AdminPortal.loadFaculty(),
            fields: [
                { key: 'name', label: 'Full Name', required: true },
                { key: 'email', label: 'Email', type: 'email', required: true },
                { key: 'phone', label: 'Phone' },
                { key: 'designation', label: 'Designation', required: true },
                { key: 'departmentId', label: 'Department', type: 'select', source: 'departments', required: true }
            ]
        },
        courses: {
            title: 'Course', idKey: 'courseId', reload: () => AdminPortal.loadCourses(),
            fields: [
                { key: 'courseName', label: 'Course Name', required: true },
                { key: 'credits', label: 'Credits', type: 'number', min: 1, max: 10, required: true },
                { key: 'courseType', label: 'Course Type', type: 'select', choices: ['Core', 'Elective', 'Lab'], required: true },
                { key: 'departmentId', label: 'Department', type: 'select', source: 'departments', required: true }
            ]
        },
        offerings: {
            title: 'Course Offering', idKey: 'offeringId', reload: () => AdminPortal.loadOfferings(),
            fields: [
                { key: 'courseId', label: 'Course', type: 'select', source: 'courses', required: true },
                { key: 'facultyId', label: 'Faculty', type: 'select', source: 'faculty', required: true },
                { key: 'semester', label: 'Semester (1-10)', type: 'number', min: 1, max: 10, required: true },
                { key: 'academicYear', label: 'Academic Year', pattern: '\\d{4}-\\d{4}', placeholder: '2026-2027', required: true },
                { key: 'section', label: 'Section', required: true },
                { key: 'roomNo', label: 'Room No' },
                { key: 'maxSeats', label: 'Max Seats', type: 'number', min: 1, required: true },
                { key: 'classesHeld', label: 'Classes Held', type: 'number', min: 0 }
            ]
        },
        exams: {
            title: 'Exam', idKey: 'examId', reload: () => AdminPortal.loadExams(),
            fields: [
                { key: 'offeringId', label: 'Course Offering', type: 'select', source: 'offerings', required: true },
                { key: 'examType', label: 'Exam Type', type: 'select', choices: ['CAT1', 'CAT2', 'FAT'], required: true },
                { key: 'examDate', label: 'Exam Date', type: 'date', required: true },
                { key: 'startTime', label: 'Start Time', type: 'time', required: true },
                { key: 'endTime', label: 'End Time', type: 'time', required: true },
                { key: 'venue', label: 'Venue', required: true },
                { key: 'maxMarks', label: 'Max Marks', type: 'number', min: 1, step: '0.5', required: true }
            ],
            // ExamCreateRequest expects "yyyy-MM-dd HH:mm:ss" for start/end (@JsonFormat)
            toPayload: d => ({ ...d, startTime: `${d.examDate} ${d.startTime}:00`, endTime: `${d.examDate} ${d.endTime}:00` })
        }
    },

    /** Edit + Delete buttons for one row. */
    actions(entity, id, label) {
        return `
            <div class="row-actions">
                <button class="btn btn-sm btn-secondary" onclick="AdminCrud.openEdit('${entity}', ${id})">Edit</button>
                <button class="btn btn-sm btn-danger" onclick="AdminCrud.confirmDelete('${entity}', ${id}, '${this.esc(label).replace(/'/g, '&#39;')}')">Delete</button>
            </div>`;
    },

    timeOf(value) {
        if (!value) return '';
        if (Array.isArray(value)) return `${String(value[3]).padStart(2, '0')}:${String(value[4] || 0).padStart(2, '0')}`;
        return String(value).substring(11, 16);
    },

    dateOf(value) {
        if (!value) return '';
        if (Array.isArray(value)) return `${value[0]}-${String(value[1]).padStart(2, '0')}-${String(value[2]).padStart(2, '0')}`;
        return String(value).substring(0, 10);
    },

    async openEdit(entity, id) {
        const schema = this.schemas[entity];
        const record = (this.cache[entity] || []).find(r => r[schema.idKey] === id);
        if (!record) {
            UI.showToast(`${schema.title} #${id} is no longer listed; refresh the page.`, 'error');
            return;
        }
        let optionLists;
        try {
            optionLists = await Promise.all(schema.fields.map(f => f.source ? this.options[f.source]() : null));
        } catch (err) {
            return; // toast already shown by API
        }

        const inputs = schema.fields.map((f, i) => {
            let value = record[f.key];
            if (f.type === 'date') value = this.dateOf(value);
            if (f.type === 'time') value = this.timeOf(value);
            const req = f.required ? 'required' : '';
            let control;
            if (f.type === 'select') {
                const opts = optionLists[i] || (f.choices || []).map(c => [c, c]);
                control = `<select id="edit-${f.key}" class="form-control" ${req}>
                    ${opts.map(([v, text]) => `<option value="${this.esc(v)}" ${String(v) === String(value) ? 'selected' : ''}>${this.esc(text)}</option>`).join('')}
                </select>`;
            } else {
                const attrs = [
                    f.min !== undefined ? `min="${f.min}"` : '',
                    f.max !== undefined ? `max="${f.max}"` : '',
                    f.step ? `step="${f.step}"` : '',
                    f.pattern ? `pattern="${f.pattern}"` : '',
                    f.placeholder ? `placeholder="${f.placeholder}"` : ''
                ].join(' ');
                control = `<input id="edit-${f.key}" type="${f.type || 'text'}" class="form-control" value="${this.esc(value)}" ${attrs} ${req} />`;
            }
            return `<div class="form-group"><label class="form-label">${f.label}</label>${control}</div>`;
        }).join('');

        UI.openModal(`
            <div class="modal-backdrop" id="crud-edit-modal">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3>Edit ${schema.title} #${id}</h3>
                        <button class="modal-close" onclick="UI.closeModal('crud-edit-modal')">&times;</button>
                    </div>
                    <form onsubmit="AdminCrud.submitEdit(event, '${entity}', ${id})">
                        <div class="modal-body">${inputs}</div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" onclick="UI.closeModal('crud-edit-modal')">Cancel</button>
                            <button type="submit" class="btn btn-primary">Save Changes</button>
                        </div>
                    </form>
                </div>
            </div>`);
    },

    async submitEdit(event, entity, id) {
        event.preventDefault();
        const schema = this.schemas[entity];
        const data = {};
        schema.fields.forEach(f => {
            const raw = document.getElementById(`edit-${f.key}`).value;
            if (f.type === 'number' || (f.type === 'select' && f.source)) {
                data[f.key] = raw === '' ? null : Number(raw);
            } else {
                data[f.key] = raw === '' ? null : raw;
            }
        });
        try {
            await API.updateEntity(entity, id, schema.toPayload ? schema.toPayload(data) : data);
            UI.closeModal('crud-edit-modal');
            schema.reload();
        } catch (err) {
            // toast already shown (400 validation / 404 / 409)
        }
    },

    async confirmDelete(entity, id, label) {
        const title = this.schemas[entity] ? this.schemas[entity].title : entity;
        if (!window.confirm(`Delete ${title} #${id}${label ? ` (${label})` : ''}?\n\nRecords with dependent academic history cannot be deleted and will be rejected.`)) {
            return;
        }
        try {
            await API.deleteEntity(entity, id);
            if (this.schemas[entity]) this.schemas[entity].reload();
            else if (entity === 'results') AdminPortal.loadResults();
            else if (entity === 'accounts') this.loadAccounts();
        } catch (err) {
            // 404 / 409 toast already shown
        }
    },

    // ---------------------------------------------------------------- Exams

    examActions(e) {
        const scheduled = e.status === 'SCHEDULED';
        return `
            <div class="row-actions">
                <button class="btn btn-sm btn-primary" onclick="AdminCrud.openResults(${e.examId})">Results</button>
                ${scheduled ? `<button class="btn btn-sm btn-success" onclick="AdminCrud.setExamStatus(${e.examId}, 'COMPLETED')">Mark Completed</button>` : ''}
                ${scheduled ? `<button class="btn btn-sm btn-secondary" onclick="AdminCrud.setExamStatus(${e.examId}, 'CANCELLED')">Cancel</button>` : ''}
                ${e.status === 'CANCELLED' ? `<button class="btn btn-sm btn-secondary" onclick="AdminCrud.setExamStatus(${e.examId}, 'SCHEDULED')">Reschedule</button>` : ''}
                <button class="btn btn-sm btn-secondary" onclick="AdminCrud.openEdit('exams', ${e.examId})">Edit</button>
                <button class="btn btn-sm btn-danger" onclick="AdminCrud.confirmDelete('exams', ${e.examId}, '${this.esc(e.courseName).replace(/'/g, '&#39;')} ${e.examType}')">Delete</button>
            </div>`;
    },

    async setExamStatus(examId, status) {
        try {
            await API.setExamStatus(examId, status);
            AdminPortal.loadExams();
        } catch (err) {
            // e.g. 409 "cannot be marked COMPLETED before it has been held"
        }
    },

    async openResults(examId) {
        const exam = (this.cache.exams || []).find(e => e.examId === examId) || { examId };
        let rows;
        try {
            rows = await API.getExamRegistrations(examId);
        } catch (err) {
            return;
        }
        const body = rows.map(r => {
            const hasResult = r.examResultId !== null && r.examResultId !== undefined;
            const active = r.status === 'REGISTERED';
            return `
                <tr>
                    <td>#${r.studentId}</td>
                    <td>${this.esc(r.studentName)}</td>
                    <td><span class="badge ${active ? 'badge-info' : 'badge-secondary'}">${r.status}</span></td>
                    <td>${hasResult ? `${r.marksObtained} / ${exam.maxMarks ?? ''} &middot; <strong>${r.gradeCode || '-'}</strong> (${r.resultStatus})` : '<span class="text-muted">Not published</span>'}</td>
                    <td>
                        ${active || hasResult ? `
                        <div class="row-actions">
                            <input type="number" id="res-marks-${r.examRegistrationId}" class="form-control form-control-sm" style="width: 90px"
                                   min="0" max="${exam.maxMarks ?? ''}" step="0.5" value="${hasResult ? r.marksObtained : ''}" placeholder="Marks" />
                            <label style="font-size: 12px; display: flex; gap: 4px; align-items: center;">
                                <input type="checkbox" id="res-withheld-${r.examRegistrationId}" ${r.resultStatus === 'WITHHELD' ? 'checked' : ''} /> Withhold
                            </label>
                            <button class="btn btn-sm btn-primary" onclick="AdminCrud.saveResult(${examId}, ${r.examRegistrationId}, ${hasResult ? r.examResultId : 'null'})">${hasResult ? 'Update' : 'Publish'}</button>
                            ${hasResult ? `<button class="btn btn-sm btn-danger" onclick="AdminCrud.deleteResult(${examId}, ${r.examResultId})">Delete</button>` : ''}
                        </div>` : ''}
                    </td>
                </tr>`;
        }).join('') || '<tr><td colspan="5" class="empty-state">No students registered for this exam.</td></tr>';

        UI.closeModal('crud-results-modal');
        UI.openModal(`
            <div class="modal-backdrop" id="crud-results-modal">
                <div class="modal-content" style="max-width: 900px;">
                    <div class="modal-header">
                        <h3>Results &middot; Exam #${examId} ${this.esc(exam.courseName || '')} ${exam.examType || ''} (${exam.status || ''})</h3>
                        <button class="modal-close" onclick="UI.closeModal('crud-results-modal')">&times;</button>
                    </div>
                    <div class="modal-body">
                        <p style="font-size: 13px; color: var(--gray-600); margin-bottom: 12px;">
                            Grade is computed by the server from the GRADE table using marks &divide; ${exam.maxMarks ?? 'Max_Marks'} &times; 100.
                            Results can only be published once the exam has ended and is marked COMPLETED.
                        </p>
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead><tr><th>Student ID</th><th>Name</th><th>Registration</th><th>Current Result</th><th>Publish / Edit</th></tr></thead>
                                <tbody>${body}</tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>`);
    },

    async saveResult(examId, registrationId, resultId) {
        const raw = document.getElementById(`res-marks-${registrationId}`).value;
        if (raw === '') {
            UI.showToast('Enter the marks obtained first.', 'error');
            return;
        }
        const withheld = document.getElementById(`res-withheld-${registrationId}`).checked;
        try {
            if (resultId) {
                await API.updateEntity('results', resultId, { marksObtained: Number(raw), withheld });
            } else {
                await API.publishResult(registrationId, Number(raw), withheld);
                UI.showToast('Result published.', 'success');
            }
            this.openResults(examId);
        } catch (err) {
            // 400 marks out of range / 409 future exam
        }
    },

    async deleteResult(examId, resultId) {
        if (!window.confirm(`Delete result #${resultId}? Results with a re-exam application cannot be deleted.`)) return;
        try {
            await API.deleteEntity('results', resultId);
            this.openResults(examId);
        } catch (err) {
            // 409 toast already shown
        }
    },

    // ---------------------------------------------------------------- User accounts

    async loadAccounts() {
        const container = document.getElementById('tab-content');
        container.innerHTML = UI.getLoadingSpinner('Loading user accounts...');
        try {
            const accounts = await API.getAccounts();
            this.cache.accounts = accounts;
            const activeCount = accounts.filter(a => a.active).length;
            container.innerHTML = `
                <div class="page-header">
                    <div class="page-title">
                        <h2>User Accounts (USER_ACCOUNT)</h2>
                        <p>${accounts.length} accounts &middot; ${activeCount} active &middot; passwords stored as BCrypt hashes only</p>
                    </div>
                    <button class="btn btn-primary" onclick="AdminPortal.loadProvisioningConsole()">+ Provision Account</button>
                </div>
                <div class="card">
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead><tr><th>Account</th><th>Username</th><th>Role</th><th>Linked Record</th><th>Status</th><th>Last Login</th><th>Actions</th></tr></thead>
                            <tbody>
                                ${accounts.map(a => `
                                    <tr>
                                        <td>#${a.accountId}</td>
                                        <td><strong>${this.esc(a.username)}</strong></td>
                                        <td><span class="badge badge-info">${a.role}</span></td>
                                        <td>${a.studentId ? `Student #${a.studentId}` : a.facultyId ? `Faculty #${a.facultyId}` : '-'}</td>
                                        <td><span class="badge ${a.active ? 'badge-success' : 'badge-danger'}">${a.active ? 'ACTIVE' : 'DISABLED'}</span></td>
                                        <td>${a.lastLogin ? this.esc(String(a.lastLogin).replace('T', ' ').substring(0, 16)) : 'Never'}</td>
                                        <td>
                                            <div class="row-actions">
                                                <button class="btn btn-sm btn-secondary" onclick="AdminCrud.toggleAccount(${a.accountId}, ${!a.active})">${a.active ? 'Deactivate' : 'Activate'}</button>
                                                <button class="btn btn-sm btn-secondary" onclick="AdminCrud.openResetPassword(${a.accountId}, '${this.esc(a.username)}')">Reset Password</button>
                                                <button class="btn btn-sm btn-danger" onclick="AdminCrud.confirmDelete('accounts', ${a.accountId}, '${this.esc(a.username)}')">Delete</button>
                                            </div>
                                        </td>
                                    </tr>`).join('') || '<tr><td colspan="7" class="empty-state">No accounts.</td></tr>'}
                            </tbody>
                        </table>
                    </div>
                </div>`;
        } catch (err) {
            container.innerHTML = UI.getErrorState('Failed to load accounts: ' + err.message);
        }
    },

    async toggleAccount(accountId, active) {
        try {
            await API.updateAccount(accountId, { active });
            this.loadAccounts();
        } catch (err) {
            // 409 last administrator
        }
    },

    openResetPassword(accountId, username) {
        UI.openModal(`
            <div class="modal-backdrop" id="crud-pwd-modal">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3>Reset Password &middot; ${username}</h3>
                        <button class="modal-close" onclick="UI.closeModal('crud-pwd-modal')">&times;</button>
                    </div>
                    <form onsubmit="AdminCrud.submitResetPassword(event, ${accountId})">
                        <div class="modal-body">
                            <div class="form-group">
                                <label class="form-label">New Password (min 6 characters)</label>
                                <input type="password" id="crud-new-password" class="form-control" minlength="6" required autocomplete="new-password" />
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" onclick="UI.closeModal('crud-pwd-modal')">Cancel</button>
                            <button type="submit" class="btn btn-primary">Set Password</button>
                        </div>
                    </form>
                </div>
            </div>`);
    },

    async submitResetPassword(event, accountId) {
        event.preventDefault();
        const newPassword = document.getElementById('crud-new-password').value;
        try {
            await API.updateAccount(accountId, { newPassword });
            UI.closeModal('crud-pwd-modal');
        } catch (err) {
            // 400 validation toast already shown
        }
    }
};
