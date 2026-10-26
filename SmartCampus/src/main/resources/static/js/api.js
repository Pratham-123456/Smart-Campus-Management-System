// Central API Client with Error Handling and JSON serialization

const API = {
    async request(url, options = {}) {
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };

        try {
            const response = await fetch(url, config);
            const data = await response.json();

            if (!response.ok || !data.success) {
                const errorMsg = data.message || `Request failed with HTTP status ${response.status}`;
                throw new Error(errorMsg);
            }

            return data.data;
        } catch (err) {
            console.error(`API Error [${url}]:`, err);
            UI.showToast(err.message || 'Network error occurred', 'error');
            throw err;
        }
    },

    get(url) {
        return this.request(url, { method: 'GET' });
    },

    post(url, body) {
        return this.request(url, {
            method: 'POST',
            body: JSON.stringify(body)
        });
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
    }
};
