// XYZ University — Academic Services Portal
// Main Application Controller, Session Security & Section Routing

const App = {
    currentUser: null,
    currentRole: null,
    currentMainSection: 'ACADEMICS', // Exactly ACADEMICS or EXAMINATIONS
    currentSubTab: null,

    async init() {
        this.checkDatabaseHealth();
        setInterval(() => this.checkDatabaseHealth(), 15000);

        // Verify if there is an active authenticated session on the backend
        try {
            const user = await API.getCurrentUser();
            if (user && user.role) {
                this.currentUser = user;
                this.showPortalView();
                this.switchMainSection('ACADEMICS');
                return;
            }
        } catch (err) {
            // Unauthenticated - expected on initial visit
        }

        // Show unauthenticated welcome page with 3-role chooser by default
        this.showWelcomeView();
    },

    async checkDatabaseHealth() {
        const dot = document.getElementById('health-dot');
        const text = document.getElementById('health-text');
        try {
            const res = await API.checkHealth();
            if (res.databaseConnected) {
                dot.className = 'health-dot online';
                text.textContent = 'Oracle 26ai: Connected';
            } else {
                dot.className = 'health-dot offline';
                text.textContent = 'Oracle 26ai: Disconnected';
            }
        } catch (err) {
            dot.className = 'health-dot offline';
            text.textContent = 'Backend: Offline';
        }
    },

    // ==========================================================================
    // UNAUTHENTICATED NAVIGATION & ROLE CHOOSER
    // ==========================================================================
    showWelcomeView() {
        this.currentUser = null;
        document.getElementById('view-welcome').style.display = 'block';
        document.getElementById('view-login').style.display = 'none';
        document.getElementById('view-portal').style.display = 'none';
        document.getElementById('app-section-nav').style.display = 'none';
        document.getElementById('app-subnav').style.display = 'none';
        document.getElementById('auth-user-badge').style.display = 'none';
    },

    async selectPortalRole(role) {
        this.currentRole = role;
        document.getElementById('login-selected-role').value = role;

        const roleBadge = document.getElementById('login-role-badge');
        const title = document.getElementById('login-title');
        const subtitle = document.getElementById('login-subtitle');
        const identLabel = document.getElementById('label-login-identifier');
        const identInput = document.getElementById('login-username');
        const pwdInput = document.getElementById('login-password');
        const errorAlert = document.getElementById('login-error-alert');
        const formLogin = document.getElementById('form-login');
        const boxAdminSetup = document.getElementById('box-admin-setup');

        // Clear previous entries & alerts
        identInput.value = '';
        pwdInput.value = '';
        pwdInput.type = 'password';
        errorAlert.style.display = 'none';
        errorAlert.textContent = '';
        boxAdminSetup.style.display = 'none';
        formLogin.style.display = 'block';

        if (role === 'STUDENT') {
            roleBadge.textContent = 'STUDENT PORTAL';
            title.textContent = 'Student Sign In';
            subtitle.textContent = 'Enter your university student credentials';
            identLabel.textContent = 'Registration Number';
            identInput.placeholder = 'e.g. 1001 or alphanumeric registration';
        } else if (role === 'FACULTY') {
            roleBadge.textContent = 'FACULTY PORTAL';
            title.textContent = 'Faculty Sign In';
            subtitle.textContent = 'Enter your university faculty / employee credentials';
            identLabel.textContent = 'Faculty / Employee ID';
            identInput.placeholder = 'e.g. 201';
        } else if (role === 'ADMINISTRATOR') {
            roleBadge.textContent = 'ADMINISTRATOR PORTAL';
            title.textContent = 'Administrator Sign In';
            subtitle.textContent = 'Enter your university administrator credentials';
            identLabel.textContent = 'Administrator ID';
            identInput.placeholder = 'e.g. admin';

            // Check if initial admin has been setup
            try {
                const status = await API.checkStatus();
                if (!status.hasAdmin) {
                    formLogin.style.display = 'none';
                    boxAdminSetup.style.display = 'block';
                }
            } catch (e) {
                // Keep standard login
            }
        }

        document.getElementById('view-welcome').style.display = 'none';
        document.getElementById('view-login').style.display = 'block';
        identInput.focus();
    },

    backToRoleSelection() {
        this.showWelcomeView();
    },

    navigateToHome() {
        if (!this.currentUser) {
            this.showWelcomeView();
        } else {
            this.switchMainSection('ACADEMICS');
        }
    },

    togglePasswordVisibility(inputId, btn) {
        const input = document.getElementById(inputId);
        if (input.type === 'password') {
            input.type = 'text';
            btn.textContent = '🙈';
        } else {
            input.type = 'password';
            btn.textContent = '👁';
        }
    },

    // ==========================================================================
    // AUTHENTICATION HANDLERS
    // ==========================================================================
    async handleLogin(event) {
        event.preventDefault();
        const errorAlert = document.getElementById('login-error-alert');
        const submitBtn = document.getElementById('btn-login-submit');

        const role = document.getElementById('login-selected-role').value;
        const username = document.getElementById('login-username').value.trim();
        const password = document.getElementById('login-password').value;

        if (!username || !password) {
            errorAlert.textContent = 'Please provide both your identification number and password.';
            errorAlert.style.display = 'block';
            return;
        }

        errorAlert.style.display = 'none';
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span>Verifying credentials...</span>';

        try {
            const resp = await API.login({ role, username, password });
            this.currentUser = resp;
            UI.showToast(`Welcome, ${resp.name}`, 'success');
            this.showPortalView();
            this.switchMainSection('ACADEMICS');
        } catch (err) {
            errorAlert.textContent = err.message || 'Authentication failed. Please verify your credentials.';
            errorAlert.style.display = 'block';
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<span>Sign In</span>';
        }
    },

    async handleAdminSetup(event) {
        event.preventDefault();
        const submitBtn = document.getElementById('btn-setup-admin');
        const username = document.getElementById('setup-admin-username').value.trim();
        const displayName = document.getElementById('setup-admin-name').value.trim();
        const password = document.getElementById('setup-admin-password').value;

        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span>Initializing...</span>';

        try {
            const resp = await API.setupAdmin({ username, displayName, password });
            this.currentUser = resp;
            UI.showToast(`Administrator account initialized for ${resp.name}`, 'success');
            this.showPortalView();
            this.switchMainSection('ACADEMICS');
        } catch (err) {
            UI.showToast(err.message || 'Failed to initialize administrator account', 'error');
        } finally {
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<span>Initialize Administrator Account</span>';
        }
    },

    async logout() {
        try {
            await API.logout();
        } catch (err) {
            // Invalidate local state regardless
        }
        this.currentUser = null;
        UI.showToast('You have been signed out successfully.', 'info');
        this.showWelcomeView();
    },

    // ==========================================================================
    // AUTHENTICATED PORTAL VIEWS & TWO MAIN SECTIONS
    // ==========================================================================
    showPortalView() {
        const u = this.currentUser;
        if (!u) return;

        document.getElementById('view-welcome').style.display = 'none';
        document.getElementById('view-login').style.display = 'none';
        document.getElementById('view-portal').style.display = 'block';
        document.getElementById('app-section-nav').style.display = 'block';
        document.getElementById('app-subnav').style.display = 'block';
        document.getElementById('auth-user-badge').style.display = 'flex';

        // Render User Badge
        document.getElementById('user-display-name').textContent = u.name || u.username;
        document.getElementById('user-display-role').textContent = u.role;
        document.getElementById('user-avatar-initial').textContent = (u.name || u.role).charAt(0).toUpperCase();
    },

    switchMainSection(section) {
        this.currentMainSection = section;

        const btnAcademics = document.getElementById('nav-btn-academics');
        const btnExams = document.getElementById('nav-btn-examinations');

        if (section === 'ACADEMICS') {
            btnAcademics.className = 'section-tab-btn active';
            btnExams.className = 'section-tab-btn';
        } else {
            btnAcademics.className = 'section-tab-btn';
            btnExams.className = 'section-tab-btn active';
        }

        this.renderSubnav();
    },

    renderSubnav() {
        const u = this.currentUser;
        if (!u) return;

        const container = document.getElementById('subnav-links-container');
        let subItems = [];

        if (this.currentMainSection === 'ACADEMICS') {
            if (u.role === 'STUDENT') {
                subItems = [
                    { id: 'student-profile', label: 'Academic Profile' },
                    { id: 'student-courses', label: 'Course Offerings & Registration' },
                    { id: 'student-attendance', label: 'Attendance Record' },
                    { id: 'student-marks', label: 'Continuous Assessment & Marks' }
                ];
            } else if (u.role === 'FACULTY') {
                subItems = [
                    { id: 'faculty-offerings', label: 'Assigned Course Offerings' },
                    { id: 'faculty-attendance', label: 'Conduct Class & Attendance' },
                    { id: 'faculty-marks', label: 'Mark Submissions' }
                ];
            } else if (u.role === 'ADMINISTRATOR') {
                subItems = [
                    { id: 'admin-dashboard', label: 'Academic Overview' },
                    { id: 'admin-departments', label: 'Departments' },
                    { id: 'admin-programs', label: 'Degree Programs' },
                    { id: 'admin-courses', label: 'Course Catalog' },
                    { id: 'admin-offerings', label: 'Course Offerings' },
                    { id: 'admin-directories', label: 'Student Directory' },
                    { id: 'admin-faculty', label: 'Faculty Directory' },
                    { id: 'admin-provisioning', label: 'Account Provisioning' },
                    { id: 'admin-accounts', label: 'User Accounts' }
                ];
            }
        } else if (this.currentMainSection === 'EXAMINATIONS') {
            if (u.role === 'STUDENT') {
                subItems = [
                    { id: 'student-exams', label: 'Exam Schedules & Registration' },
                    { id: 'student-results', label: 'Published Results & Grades' },
                    { id: 'student-reexams', label: 'Re-Examination Applications' }
                ];
            } else if (u.role === 'FACULTY') {
                subItems = [
                    { id: 'faculty-exams', label: 'Invigilation Duties & Schedules' }
                ];
            } else if (u.role === 'ADMINISTRATOR') {
                subItems = [
                    { id: 'admin-exams', label: 'Schedule Examinations' },
                    { id: 'admin-results', label: 'Published Results' },
                    { id: 'admin-reexams', label: 'Re-Exam Decisions' }
                ];
            }
        }

        container.innerHTML = subItems.map((item, idx) => `
            <button class="subnav-link ${idx === 0 ? 'active' : ''}" 
                    id="subnav-btn-${item.id}"
                    onclick="App.navigateToSubTab('${item.id}')">
                ${item.label}
            </button>
        `).join('');

        if (subItems.length > 0) {
            this.navigateToSubTab(subItems[0].id);
        }
    },

    navigateToSubTab(tabId) {
        this.currentSubTab = tabId;

        // Highlight active subnav link
        document.querySelectorAll('.subnav-link').forEach(btn => {
            btn.className = 'subnav-link';
        });
        const activeBtn = document.getElementById(`subnav-btn-${tabId}`);
        if (activeBtn) activeBtn.className = 'subnav-link active';

        const u = this.currentUser;
        if (!u) return;

        switch (tabId) {
            // Student Academics
            case 'student-profile': StudentPortal.loadProfile(u.id); break;
            case 'student-courses': StudentPortal.loadCourses(u.id); break;
            case 'student-attendance': StudentPortal.loadAttendance(u.id); break;
            case 'student-marks': StudentPortal.loadMarks(u.id); break;

            // Student Examinations
            case 'student-exams': StudentPortal.loadExams(u.id); break;
            case 'student-results': StudentPortal.loadResults(u.id); break;
            case 'student-reexams': StudentPortal.loadReExams(u.id); break;

            // Faculty Academics
            case 'faculty-offerings': FacultyPortal.loadOfferings(u.id); break;
            case 'faculty-attendance': FacultyPortal.loadOfferings(u.id); break;
            case 'faculty-marks': FacultyPortal.loadOfferings(u.id); break;

            // Faculty Examinations
            case 'faculty-exams': FacultyPortal.loadExams(u.id); break;

            // Administrator Academics
            case 'admin-dashboard': AdminPortal.loadDashboard(); break;
            case 'admin-departments': AdminPortal.loadDepartments(); break;
            case 'admin-programs': AdminPortal.loadPrograms(); break;
            case 'admin-courses': AdminPortal.loadCourses(); break;
            case 'admin-offerings': AdminPortal.loadOfferings(); break;
            case 'admin-directories': AdminPortal.loadStudents(); break;
            case 'admin-faculty': AdminPortal.loadFaculty(); break;
            case 'admin-provisioning': AdminPortal.loadProvisioningConsole(); break;
            case 'admin-accounts': AdminCrud.loadAccounts(); break;

            // Administrator Examinations
            case 'admin-exams': AdminPortal.loadExams(); break;
            case 'admin-results': AdminPortal.loadResults(); break;
            case 'admin-reexams': AdminPortal.loadReExams(); break;

            default:
                if (u.role === 'STUDENT') StudentPortal.loadProfile(u.id);
                else if (u.role === 'FACULTY') FacultyPortal.loadOfferings(u.id);
                else AdminPortal.loadDashboard();
        }
    }
};

// UI Toast & Modal Helpers
const UI = {
    showToast(message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        
        let icon = 'ℹ️';
        if (type === 'success') icon = '✓';
        if (type === 'error') icon = '⚠️';

        const iconSpan = document.createElement('span');
        iconSpan.textContent = icon;
        const textSpan = document.createElement('span');
        textSpan.textContent = message; // server text is never interpreted as HTML
        toast.append(iconSpan, textSpan);
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    },

    openModal(html) {
        this.closeAllModals();
        const div = document.createElement('div');
        div.innerHTML = html;
        document.body.appendChild(div.firstElementChild);
    },

    closeModal(modalId) {
        const el = document.getElementById(modalId);
        if (el) el.remove();
    },

    closeAllModals() {
        document.querySelectorAll('.modal-overlay').forEach(m => m.remove());
    },

    getLoadingSpinner(msg = 'Retrieving university records...') {
        return `
            <div style="text-align: center; padding: 60px 20px; color: var(--color-text-muted);">
                <div style="font-size: 28px; animation: spin 1s infinite linear; display: inline-block;">⏳</div>
                <div style="margin-top: 12px; font-weight: 600; font-size: 13.5px;">${msg}</div>
            </div>
            <style>
                @keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); } }
            </style>
        `;
    },

    getErrorState(msg) {
        return `
            <div class="empty-state">
                <div class="empty-state-icon">⚠️</div>
                <h3>Unable to Load Records</h3>
                <p>${msg}</p>
                <button class="btn btn-secondary btn-sm" style="margin-top: 14px;" onclick="App.switchMainSection(App.currentMainSection)">
                    Retry
                </button>
            </div>
        `;
    },

    filterTable(tableSelector, query) {
        const q = (query || '').toLowerCase().trim();
        const table = document.querySelector(tableSelector);
        if (!table) return;
        const rows = table.querySelectorAll('tbody tr');
        rows.forEach(r => {
            if (r.querySelector('.empty-state') || r.classList.contains('empty-row')) return;
            const text = r.textContent.toLowerCase();
            r.style.display = text.includes(q) ? '' : 'none';
        });
    }
};

// Start application on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    App.init();
});
