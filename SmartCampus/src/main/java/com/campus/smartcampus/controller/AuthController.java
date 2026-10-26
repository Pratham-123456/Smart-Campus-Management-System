package com.campus.smartcampus.controller;

import com.campus.smartcampus.dto.AccountProvisionRequest;
import com.campus.smartcampus.dto.LoginRequest;
import com.campus.smartcampus.dto.LoginResponse;
import com.campus.smartcampus.dto.SetupAdminRequest;
import com.campus.smartcampus.model.ApiResponse;
import com.campus.smartcampus.repository.CommonRepository;
import com.campus.smartcampus.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CommonRepository commonRepository;

    public AuthController(AuthService authService, CommonRepository commonRepository) {
        this.authService = authService;
        this.commonRepository = commonRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session) {
        LoginResponse response = authService.login(request, session);
        return ResponseEntity.ok(ApiResponse.ok("Sign in successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginResponse>> getCurrentUser(HttpSession session) {
        LoginResponse response = authService.getCurrentUser(session);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(ApiResponse.ok("Signed out successfully", null));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus() {
        return ResponseEntity.ok(ApiResponse.ok(authService.getSetupStatus()));
    }

    @PostMapping("/setup-admin")
    public ResponseEntity<ApiResponse<LoginResponse>> setupInitialAdmin(
            @Valid @RequestBody SetupAdminRequest request,
            HttpSession session) {
        LoginResponse response = authService.setupInitialAdmin(request, session);
        return ResponseEntity.ok(ApiResponse.ok("System administrator account provisioned successfully", response));
    }

    @PostMapping("/provision")
    public ResponseEntity<ApiResponse<String>> provisionAccount(
            @Valid @RequestBody AccountProvisionRequest request,
            HttpSession session) {
        authService.provisionAccount(request, session);
        return ResponseEntity.ok(ApiResponse.ok("User account provisioned successfully for " + request.getUsername(), null));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        boolean dbOk = commonRepository.testConnection();
        Map<String, Object> status = Map.of(
                "institution", "XYZ University",
                "portal", "Academic Services Portal",
                "databaseConnected", dbOk,
                "databaseEngine", "Oracle Database 26ai Free (FREEPDB1)"
        );
        return ResponseEntity.ok(ApiResponse.ok(status));
    }
}
