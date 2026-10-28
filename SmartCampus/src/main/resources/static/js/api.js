// Central API Client with Error Handling and JSON serialization

const API = {
    // Number of write requests in flight; while > 0 the body carries .api-busy and
    // every action button is disabled, which prevents double submission.
    pendingWrites: 0,

    async request(url, options = {}) {
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };
        const isWrite = config.method && config.method !== 'GET';
        if (isWrite) this.setBusy(+1);

        try {
            const response = await fetch(url, config);
            let data = null;
            try {
                data = await response.json();
            } catch (parseErr) {
                data = null; // empty or non-JSON body
            }

            if (!response.ok || (data && data.success === false)) {
                const errorMsg = (data && data.message) || `Request failed with HTTP status ${response.status}`;
                const error = new Error(errorMsg);
                error.status = response.status;
                throw error;
            }

            // POST callers show their own success toast; PUT/PATCH/DELETE use the server message.
            if (options.toast && data && data.message) {
                UI.showToast(data.message, 'success');
            }
            return data ? data.data : null;
        } catch (err) {
            console.error(`API Error [${url}]:`, err);
            const prefix = err.status ? `[${err.status}] ` : '';
            UI.showToast(prefix + (err.message || 'Network error occurred'), 'error');
            throw err;
        } finally {
            if (isWrite) this.setBusy(-1);
        }
    },

    setBusy(delta) {
        this.pendingWrites = Math.max(0, this.pendingWrites + delta);
        const busy = this.pendingWrites > 0;
        document.body.classList.toggle('api-busy', busy);
        document.querySelectorAll('button.btn, button[type="submit"]').forEach(btn => {
            if (busy) {
                if (!btn.disabled) { btn.disabled = true; btn.dataset.busyLocked = '1'; }
            } else if (btn.dataset.busyLocked) {
                btn.disabled = false;
                delete btn.dataset.busyLocked;
            }
        });
    },

    get(url) {
        return this.request(url, { method: 'GET' });
    },

    post(url, body, options = {}) {
        return this.request(url, {
            ...options,
            method: 'POST',
            body: JSON.stringify(body)
        });
    },

    put(url, body) {
        return this.request(url, { method: 'PUT', body: JSON.stringify(body), toast: true });
    },

    patch(url, body) {
        return this.request(url, { method: 'PATCH', body: JSON.stringify(body), toast: true });
    },

    del(url) {
        return this.request(url, { method: 'DELETE', toast: true });
    },

    // Health & Setup
    checkHealth() {
        return this.get('/api/auth/health');
    },
    checkStatus() {
        return this.get('/api/auth/status');
    },

    // Auth
    login(credentials) {
        return this.post('/api/auth/login', credentials);
    },
    getCurrentUser() {
        return this.get('/api/auth/me');
    },
    logout() {
        return this.post('/api/auth/logout', {});
    },
    setupAdmin(data) {
        return this.post('/api/auth/setup-admin', data);
    },
    provisionAccount(data) {
        return this.post('/api/auth/provision', data);
    },

    // Student
    getStudentProfile(id) {
        return this.get(`/api/student/${id}/profile`);
    },
    getStudentEnrollments(id) {
        return this.get(`/api/student/${id}/enrollments`);
    },
    getStudentOfferings(id) {
        return this.get(`/api/student/${id}/offerings`);
    },
    enrollCourse(studentId, offeringId) {
        return this.post(`/api/student/${studentId}/enroll`, { offeringId });
    },
    getStudentAttendance(id) {
        return this.get(`/api/student/${id}/attendance`);
    },
    getStudentMarks(id) {
        return this.get(`/api/student/${id}/marks`);
    },
    getStudentExams(id) {
        return this.get(`/api/student/${id}/exams`);
    },
    registerExam(studentId, examId) {
        return this.post(`/api/student/${studentId}/exams/register`, { examId });
    },
    getStudentResults(id) {
        return this.get(`/api/student/${id}/results`);
    },
    getStudentReExams(id) {
        return this.get(`/api/student/${id}/re-exams`);
    },
    applyReExam(studentId, examResultId, reason) {
        return this.post(`/api/student/${studentId}/re-exams/apply`, { examResultId, reason });
    },

    // Faculty
    getFacultyProfile(id) {
        return this.get(`/api/faculty/${id}/profile`);
    },
    getFacultyOfferings(id) {
        return this.get(`/api/faculty/${id}/offerings`);
    },
    getEnrolledStudents(facultyId, offeringId) {
        return this.get(`/api/faculty/${facultyId}/offerings/${offeringId}/students`);
    },
    updateClassesHeld(facultyId, offeringId, classesHeld) {
        return this.post(`/api/faculty/${facultyId}/offerings/${offeringId}/classes-held`, { classesHeld });
    },
    updateAttendance(facultyId, payload) {
        return this.post(`/api/faculty/${facultyId}/attendance`, payload);
    },
    updateMarks(facultyId, payload) {
        return this.post(`/api/faculty/${facultyId}/marks`, payload);
    },
    getFacultyExams(id) {
        return this.get(`/api/faculty/${id}/exams`);
    },

    // Admin
    getAdminStats() { return this.get('/api/admin/stats'); },
    getDepartments() { return this.get('/api/admin/departments'); },
    createDepartment(data) { return this.post('/api/admin/departments', data); },

    getPrograms() { return this.get('/api/admin/programs'); },
    createProgram(data) { return this.post('/api/admin/programs', data); },

    getStudents() { return this.get('/api/admin/students'); },
    createStudent(data) { return this.post('/api/admin/students', data); },

    getFaculty() { return this.get('/api/admin/faculty'); },
    createFaculty(data) { return this.post('/api/admin/faculty', data); },

    getCourses() { return this.get('/api/admin/courses'); },
    createCourse(data) { return this.post('/api/admin/courses', data); },

    getOfferings() { return this.get('/api/admin/offerings'); },
    createOffering(data) { return this.post('/api/admin/offerings', data); },

    getAdminExams() { return this.get('/api/admin/exams'); },
    createExam(data) { return this.post('/api/admin/exams', data); },

    getAdminResults() { return this.get('/api/admin/results'); },
    getAdminReExams() { return this.get('/api/admin/re-exams'); },
    decideReExam(applicationId, decision) {
        return this.post('/api/admin/re-exams/decision', { applicationId, decision });
    },

    // Phase 3: aliases used by the provisioning console
    getAdminStudents() { return this.getStudents(); },
    getAdminFaculty() { return this.getFaculty(); },

    // Phase 3: generic admin update/delete — entity is the REST collection name
    // (departments, programs, students, faculty, courses, offerings, exams, results, accounts)
    updateEntity(entity, id, data) { return this.put(`/api/admin/${entity}/${id}`, data); },
    deleteEntity(entity, id) { return this.del(`/api/admin/${entity}/${id}`); },
    setExamStatus(examId, status) { return this.patch(`/api/admin/exams/${examId}/status`, { status }); },
    getExamRegistrations(examId) { return this.get(`/api/admin/exams/${examId}/registrations`); },
    publishResult(examRegistrationId, marksObtained, withheld) {
        return this.post('/api/admin/results', { examRegistrationId, marksObtained, withheld });
    },
    getGrades() { return this.get('/api/admin/grades'); },
    getAccounts() { return this.get('/api/admin/accounts'); },
    updateAccount(accountId, data) { return this.patch(`/api/admin/accounts/${accountId}`, data); },

    // Phase 3: student cancellations
    dropCourse(studentId, offeringId) { return this.del(`/api/student/${studentId}/enrollments/${offeringId}`); },
    cancelExamRegistration(studentId, examId) { return this.del(`/api/student/${studentId}/exams/${examId}/registration`); },
    withdrawReExam(studentId, applicationId) { return this.del(`/api/student/${studentId}/re-exams/${applicationId}`); }
};
