# Phase 3 Final Presentation — 15-slide outline

(A new deck; the Review 2 PPT is not modified. Screenshot file names refer to `docs/phase3/screenshots/`.)

| # | Title | Content | Visual |
|---|---|---|---|
| 1 | Smart Campus Management System — Phase 3: Integration & Delivery | BACSE202 Fall 2026 · Pratham Garg 25BCE2016 · S Rohith 25BCE0790 · Divyam Kurkan 25BCE2041 | University logo |
| 2 | Problem & Objectives | Fragmented academic records → one constraint-checked source of truth; objectives 1–4 (report §4) | Three role icons |
| 3 | Architecture | SPA → AuthInterceptor → Controllers → Services → JDBC → HikariCP → Oracle 26ai | Layer diagram (report §7) |
| 4 | Schema: 15 tables, 20 FKs | ER diagram; 14 academic tables + USER_ACCOUNT | ER diagram |
| 5 | Normalisation | 1NF → 2NF → 3NF; Classes_Held moved to COURSE_OFFERING; no Student_ID in RE_EXAM_APPLICATION; attendance % computed | Before/after FD sketch |
| 6 | Review 2 Appendix A: 8 findings, all closed | Table from report §13 (Code / Live DB / Verified) | Checkmark table |
| 7 | Data quality: before → after | 3 grades, 51 duplicate groups, 102 clashes, 115 + 24 + 3 timeline issues → **0**; 1,712 seeded vs 1,736 live | Bar chart from `sql_audit_*` |
| 8 | Constraints in Oracle | Semester NUMBER(2) 1–10; 5 status CHECKs; marks 30/30/50; probes 23/23 | Probe output excerpt |
| 9 | Marks policy: total out of 100 | Components 30 + 30 + 50; total = sum × 100 ÷ 110 (never above 100); grade from GRADE | `05-student-marks-grade-history.png` |
| 10 | Business rules | Cross-section 409, clash 409 (back-to-back allowed), future exam 409 | `09-toast-409…png`, `21-toast-409…png` |
| 11 | CRUD on all 15 tables | Matrix summary; logical retirement (DROPPED, CANCELLED) vs FK-protected delete (409) | `16-admin-offerings-crud-actions.png`, `18-toast-409…png` |
| 12 | Student portal | Attendance %, marks/grade history, published results, re-exam (Review 2 Slide 10 gaps) | Screenshots 04–08 |
| 13 | Faculty & admin portals | Roster totals /100, marks modal; KPIs; result publication with auto-grade | Screenshots 12, 13, 15, 20 |
| 14 | Testing & evidence | JUnit 36/36 on live Oracle (rolled back); workflow suite 46/46 with unchanged data; SQL audit 0 violations | Test summary table |
| 15 | Conclusion & future work | Delivered scope; future: trigger for cross-section rule, sequences, CGPA, audit log; Q&A | — |

**Speaker split (suggested):** Pratham — slides 1–5 and 15 (design and normalisation); Rohith — 6–10 (findings, constraints, rules); Divyam — 11–14 (CRUD, UI, testing).
