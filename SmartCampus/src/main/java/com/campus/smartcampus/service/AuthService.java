package com.campus.smartcampus.service;

import com.campus.smartcampus.dto.AccountProvisionRequest;
import com.campus.smartcampus.dto.LoginRequest;
import com.campus.smartcampus.dto.LoginResponse;
import com.campus.smartcampus.dto.SetupAdminRequest;
import com.campus.smartcampus.exception.ConflictException;
import com.campus.smartcampus.exception.ForbiddenException;
import com.campus.smartcampus.exception.NotFoundException;
import com.campus.smartcampus.model.UserAccount;
import com.campus.smartcampus.repository.CommonRepository;
import com.campus.smartcampus.repository.UserAccountRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    public static final String SESSION_AUTH_KEY = "AUTH_USER";

    private final UserAccountRepository userAccountRepository;
    private final CommonRepository commonRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserAccountRepository userAccountRepository, CommonRepository commonRepository) {
        this.userAccountRepository = userAccountRepository;
        this.commonRepository = commonRepository;
    }

    public LoginResponse login(LoginRequest request, HttpSession session) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username or identifier is required.");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        String rawRole = request.getRole() != null ? request.getRole().toUpperCase().trim() : "";
        String expectedRole = switch (rawRole) {
            case "ADMIN", "ADMINISTRATOR" -> "ADMINISTRATOR";
            case "FACULTY" -> "FACULTY";
            case "STUDENT" -> "STUDENT";
            default -> throw new IllegalArgumentException("Unrecognized portal role: " + rawRole);
        };

        UserAccount account = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials or account does not exist."));

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials or password.");
        }

        if (!expectedRole.equalsIgnoreCase(account.getRole())) {
            throw new IllegalArgumentException("Account is registered as " + account.getRole() + 
                    ", but you attempted to sign in via the " + rawRole + " portal.");
        }

        if (account.getIsActive() != 1) {
            throw new IllegalStateException("Your account has been deactivated. Please contact university administration.");
        }

        userAccountRepository.updateLastLogin(account.getAccountId());

        LoginResponse response = new LoginResponse();
        response.setAccountId(account.getAccountId());
        response.setUsername(account.getUsername());
        response.setRole(account.getRole());
        response.setToken(UUID.randomUUID().toString());

        if ("STUDENT".equalsIgnoreCase(account.getRole())) {
            response.setId(account.getStudentId());
            commonRepository.getStudentLoginDetails(account.getStudentId()).ifPresent(s -> {
                response.setName(s.getName());
                response.setEmail(s.getEmail());
                response.setProgramName(s.getProgramName());
                response.setDepartmentName(s.getDepartmentName());
            });
        } else if ("FACULTY".equalsIgnoreCase(account.getRole())) {
            response.setId(account.getFacultyId());
            commonRepository.getFacultyLoginDetails(account.getFacultyId()).ifPresent(f -> {
                response.setName(f.getName());
                response.setEmail(f.getEmail());
                response.setDepartmentName(f.getDepartmentName());
                response.setProgramName("Department of " + f.getDepartmentName());
            });
        } else {
            response.setId(account.getAccountId());
            response.setName("System Administrator");
            response.setEmail("admin@xyz.edu");
            response.setDepartmentName("Office of Academic Administration");
            response.setProgramName("University Information System");
        }

        session.setAttribute(SESSION_AUTH_KEY, response);
        return response;
    }

    public LoginResponse getCurrentUser(HttpSession session) {
        if (session == null) {
            throw new IllegalStateException("No active session found.");
        }
        LoginResponse user = (LoginResponse) session.getAttribute(SESSION_AUTH_KEY);
        if (user == null) {
            throw new IllegalStateException("Not authenticated. Please sign in.");
        }
        return user;
    }

    public void logout(HttpSession session) {
        if (session != null) {
            session.removeAttribute(SESSION_AUTH_KEY);
            session.invalidate();
        }
    }

    public Map<String, Object> getSetupStatus() {
        int adminCount = userAccountRepository.countAdmins();
        return Map.of(
                "hasAdmin", adminCount > 0,
                "adminCount", adminCount
        );
    }

    public LoginResponse setupInitialAdmin(SetupAdminRequest req, HttpSession session) {
        if (userAccountRepository.countAdmins() > 0) {
            throw new IllegalStateException("System administrator account is already provisioned. Initial setup is locked.");
        }

        if (userAccountRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        UserAccount admin = new UserAccount();
        admin.setUsername(req.getUsername().trim());
        admin.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        admin.setRole("ADMINISTRATOR");
        admin.setIsActive(1);
        userAccountRepository.createAccount(admin);

        // Immediately authenticate as administrator
        LoginRequest loginReq = new LoginRequest("ADMINISTRATOR", req.getUsername(), req.getPassword());
        return login(loginReq, session);
    }

    public void provisionAccount(AccountProvisionRequest req, HttpSession session) {
        LoginResponse current = getCurrentUser(session);
        if (!"ADMINISTRATOR".equalsIgnoreCase(current.getRole())) {
            throw new ForbiddenException("Access denied: Only administrators can provision user accounts.");
        }

        String role = req.getRole().toUpperCase().trim();
        if (!role.equals("STUDENT") && !role.equals("FACULTY")) {
            throw new IllegalArgumentException("Provisioning role must be STUDENT or FACULTY.");
        }

        if (userAccountRepository.existsByUsername(req.getUsername())) {
            throw new ConflictException("Username '" + req.getUsername() + "' is already assigned to an account.");
        }
        boolean recordExists = "STUDENT".equals(role)
                ? commonRepository.getStudentLoginDetails(req.getRecordId()).isPresent()
                : commonRepository.getFacultyLoginDetails(req.getRecordId()).isPresent();
        if (!recordExists) {
            throw new NotFoundException(role.charAt(0) + role.substring(1).toLowerCase() + " ID " + req.getRecordId() + " does not exist.");
        }
        if (userAccountRepository.existsForRecord(role, req.getRecordId())) {
            throw new ConflictException(role.charAt(0) + role.substring(1).toLowerCase() + " ID " + req.getRecordId()
                    + " already has an account (it may be deactivated).");
        }

        UserAccount acc = new UserAccount();
        acc.setUsername(req.getUsername().trim());
        acc.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        acc.setRole(role);
        acc.setIsActive(1);

        if ("STUDENT".equals(role)) {
            if (userAccountRepository.findByStudentId(req.getRecordId()).isPresent()) {
                throw new IllegalArgumentException("Student ID " + req.getRecordId() + " already has an account provisioned.");
            }
            acc.setStudentId(req.getRecordId());
        } else {
            if (userAccountRepository.findByFacultyId(req.getRecordId()).isPresent()) {
                throw new IllegalArgumentException("Faculty ID " + req.getRecordId() + " already has an account provisioned.");
            }
            acc.setFacultyId(req.getRecordId());
        }

        userAccountRepository.createAccount(acc);
    }
}
