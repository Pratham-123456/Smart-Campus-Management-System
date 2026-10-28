// Captures genuine screenshots of the running portal for the Phase 3 report.
//
//   node scripts/capture_screenshots.js            (app must be running on :8080)
//
// No npm packages needed: it starts headless Chrome/Edge and drives it over the
// Chrome DevTools Protocol with Node's built-in WebSocket (Node 22+). Screens that
// show an error toast are produced by requests the server REJECTS (409/400/403), so
// capturing never changes data. Edit modals are opened but never submitted.
// Output: docs/phase3/screenshots/NN-name.png

const { spawn } = require('child_process');
const fs = require('fs');
const os = require('os');
const path = require('path');

const BASE = process.env.PORTAL_URL || 'http://localhost:8080';
const OUT = path.join(__dirname, '..', 'docs', 'phase3', 'screenshots');
const PORT = 9333;
const BROWSERS = [
    'C:/Program Files/Google/Chrome/Application/chrome.exe',
    'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe',
    '/usr/bin/google-chrome', '/usr/bin/chromium', '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'
];

const sleep = ms => new Promise(r => setTimeout(r, ms));

// Dev seed logins (all seeded accounts use the dev password 1234)
const LOGINS = {
    student: { username: '1001', password: '1234', role: 'STUDENT' },
    faculty: { username: '201', password: '1234', role: 'FACULTY' },
    admin: { username: 'admin', password: '1234', role: 'ADMINISTRATOR' }
};

const tab = (section, id) => `App.switchMainSection('${section}'); App.navigateToSubTab('${id}');`;

// [file, login (or null for logged-out), JS to run, wait ms]
const SHOTS = [
    ['01-login-role-selection', null, '', 1500],
    ['02-student-dashboard-profile', 'student', tab('ACADEMICS', 'student-profile'), 2000],
    ['03-student-course-registration-seats', 'student', tab('ACADEMICS', 'student-courses'), 2000],
    ['04-student-attendance-percentage', 'student', tab('ACADEMICS', 'student-attendance'), 2000],
    ['05-student-marks-grade-history', 'student', tab('ACADEMICS', 'student-marks'), 2000],
    ['06-student-exam-schedule-registration', 'student', tab('EXAMINATIONS', 'student-exams'), 2000],
    ['07-student-published-results', 'student', tab('EXAMINATIONS', 'student-results'), 2000],
    ['08-student-reexam-applications', 'student', tab('EXAMINATIONS', 'student-reexams'), 2000],
    // 1001 is active in offering 401 (Database Systems, Sec A); 404 is Sec B of the same course and term
    ['09-toast-409-cross-section-enrollment', 'student', tab('ACADEMICS', 'student-courses') + " setTimeout(() => API.enrollCourse(1001, 404).catch(() => {}), 1200);", 2600],
    ['10-toast-403-student-calls-admin-api', 'student', tab('ACADEMICS', 'student-profile') + " setTimeout(() => API.get('/api/admin/stats').catch(() => {}), 1200);", 2600],
    ['11-faculty-assigned-offerings', 'faculty', tab('ACADEMICS', 'faculty-offerings'), 2000],
    ['12-faculty-roster-attendance-marks', 'faculty', "FacultyPortal.loadStudents(201, 401, 'Database Systems', 'A');", 2200],
    ['13-faculty-marks-entry-modal', 'faculty', "FacultyPortal.loadStudents(201, 401, 'Database Systems', 'A'); setTimeout(() => FacultyPortal.openMarksModal(201, 401, 1001, 'Aarav Sharma', 18, 24, 42), 1500);", 2400],
    ['14-toast-400-marks-above-ceiling', 'faculty', "FacultyPortal.loadStudents(201, 401, 'Database Systems', 'A'); setTimeout(() => API.updateMarks(201, { studentId: 1001, offeringId: 401, continuousAssessmentMarks: 35, midtermMarks: 20, finalExamMarks: 40 }).catch(() => {}), 1500);", 2600],
    ['15-admin-dashboard-live-kpis', 'admin', tab('ACADEMICS', 'admin-dashboard'), 2200],
    ['16-admin-offerings-crud-actions', 'admin', tab('ACADEMICS', 'admin-offerings'), 2200],
    ['17-admin-edit-offering-modal', 'admin', tab('ACADEMICS', 'admin-offerings') + " setTimeout(() => AdminCrud.openEdit('offerings', 401), 1500);", 3000],
    ['18-toast-409-delete-department-with-children', 'admin', tab('ACADEMICS', 'admin-departments') + " setTimeout(() => API.deleteEntity('departments', 1).catch(() => {}), 1500);", 2800],
    ['19-admin-exam-timetable-actions', 'admin', tab('EXAMINATIONS', 'admin-exams'), 2200],
    ['20-admin-result-publication-modal', 'admin', tab('EXAMINATIONS', 'admin-exams') + " setTimeout(() => AdminCrud.openResults(801), 1500);", 3000],
    ['21-toast-409-complete-future-exam', 'admin', tab('EXAMINATIONS', 'admin-exams') + " setTimeout(() => API.setExamStatus(812, 'COMPLETED').catch(() => {}), 1500);", 2800],
    ['22-admin-reexam-decisions', 'admin', tab('EXAMINATIONS', 'admin-reexams'), 2200],
    ['23-admin-user-accounts', 'admin', tab('ACADEMICS', 'admin-accounts'), 2200]
];

class Cdp {
    constructor(ws) {
        this.ws = ws; this.id = 0; this.pending = new Map(); this.listeners = [];
        ws.addEventListener('message', ev => {
            const msg = JSON.parse(ev.data);
            if (msg.id && this.pending.has(msg.id)) {
                const { resolve, reject } = this.pending.get(msg.id);
                this.pending.delete(msg.id);
                msg.error ? reject(new Error(msg.error.message)) : resolve(msg.result);
            } else if (msg.method) {
                this.listeners.forEach(l => l(msg));
            }
        });
    }
    send(method, params = {}) {
        const id = ++this.id;
        this.ws.send(JSON.stringify({ id, method, params }));
        return new Promise((resolve, reject) => this.pending.set(id, { resolve, reject }));
    }
    async eval(expression) {
        const r = await this.send('Runtime.evaluate', { expression, awaitPromise: true, returnByValue: true });
        if (r.exceptionDetails) throw new Error(r.exceptionDetails.text + ' ' + (r.exceptionDetails.exception?.description || ''));
        return r.result.value;
    }
}

async function main() {
    const exe = BROWSERS.find(p => fs.existsSync(p));
    if (!exe) throw new Error('Chrome/Edge not found; set one of: ' + BROWSERS.join(', '));
    fs.mkdirSync(OUT, { recursive: true });
    const profile = fs.mkdtempSync(path.join(os.tmpdir(), 'portal-shots-'));
    const browser = spawn(exe, ['--headless=new', `--remote-debugging-port=${PORT}`, `--user-data-dir=${profile}`,
        '--window-size=1440,960', '--hide-scrollbars', '--no-first-run', 'about:blank'], { stdio: 'ignore' });

    let target;
    for (let i = 0; i < 40 && !target; i++) {
        await sleep(250);
        try { target = (await (await fetch(`http://127.0.0.1:${PORT}/json/list`)).json()).find(t => t.type === 'page'); } catch { /* not up yet */ }
    }
    if (!target) throw new Error('Browser did not expose a DevTools page');

    const ws = new WebSocket(target.webSocketDebuggerUrl);
    await new Promise((res, rej) => { ws.addEventListener('open', res); ws.addEventListener('error', rej); });
    const cdp = new Cdp(ws);
    await cdp.send('Page.enable');
    await cdp.send('Emulation.setDeviceMetricsOverride', { width: 1440, height: 960, deviceScaleFactor: 1, mobile: false });
    // Auto-accept confirm()/alert() so nothing blocks
    cdp.listeners.push(m => { if (m.method === 'Page.javascriptDialogOpening') cdp.send('Page.handleJavaScriptDialog', { accept: true }); });

    const goto = async url => { await cdp.send('Page.navigate', { url }); await sleep(1800); };
    let current = undefined;
    const results = [];

    for (const [name, who, script, wait] of SHOTS) {
        if (who !== current) {
            await goto(BASE + '/');
            await cdp.eval("fetch('/api/auth/logout', { method: 'POST' }).catch(() => {})");
            if (who) {
                const ok = await cdp.eval(`fetch('/api/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(${JSON.stringify(LOGINS[who])}) }).then(r => r.ok)`);
                if (!ok) throw new Error(`login failed for ${who}`);
            }
            await goto(BASE + '/');
            current = who;
        }
        await cdp.eval("document.querySelectorAll('.modal-backdrop, .toast').forEach(e => e.remove()); window.scrollTo(0, 0); true");
        if (script) await cdp.eval(`(() => { ${script} return true; })()`);
        await sleep(wait);
        const height = Math.min(2400, await cdp.eval('Math.max(document.body.scrollHeight, 960)'));
        const shot = await cdp.send('Page.captureScreenshot', { format: 'png', captureBeyondViewport: true, clip: { x: 0, y: 0, width: 1440, height, scale: 1 } });
        const file = path.join(OUT, `${name}.png`);
        fs.writeFileSync(file, Buffer.from(shot.data, 'base64'));
        results.push(`${name}.png`);
        console.log('saved', path.relative(process.cwd(), file));
    }

    ws.close();
    browser.kill();
    console.log(`\n${results.length} screenshots written to ${path.relative(process.cwd(), OUT)}`);
}

main().catch(err => { console.error(err); process.exit(1); });
