package com.campus.smartcampus.config;

import com.campus.smartcampus.dto.LoginResponse;
import com.campus.smartcampus.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();

        // Allow all public authentication and static resource endpoints
        if (uri.startsWith("/api/auth") || uri.startsWith("/css") || uri.startsWith("/js") || 
            uri.startsWith("/images") || uri.equals("/") || uri.equals("/index.html") || uri.startsWith("/error")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        LoginResponse user = (session != null) ? (LoginResponse) session.getAttribute(AuthService.SESSION_AUTH_KEY) : null;

        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized: Please sign in with valid credentials.\",\"data\":null}");
            return false;
        }

        String role = user.getRole();

        // Student endpoint protection
        if (uri.startsWith("/api/student")) {
            if ("STUDENT".equalsIgnoreCase(role)) {
                // Enforce that student can only access their own records
                String[] parts = uri.split("/");
                if (parts.length >= 4) {
                    try {
                        long requestedStudentId = Long.parseLong(parts[3]);
                        if (user.getId() != null && user.getId() != requestedStudentId) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"success\":false,\"message\":\"Access Denied: You cannot access records belonging to another student.\",\"data\":null}");
                            return false;
                        }
                    } catch (NumberFormatException ignored) {}
                }
                return true;
            } else if ("ADMINISTRATOR".equalsIgnoreCase(role)) {
                return true; // Admin has read access for verification
            }
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Access Denied: Student portal credentials required.\",\"data\":null}");
            return false;
        }

        // Faculty endpoint protection
        if (uri.startsWith("/api/faculty")) {
            if ("FACULTY".equalsIgnoreCase(role)) {
                String[] parts = uri.split("/");
                if (parts.length >= 4) {
                    try {
                        long requestedFacultyId = Long.parseLong(parts[3]);
                        if (user.getId() != null && user.getId() != requestedFacultyId) {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"success\":false,\"message\":\"Access Denied: You cannot access offerings belonging to another faculty member.\",\"data\":null}");
                            return false;
                        }
                    } catch (NumberFormatException ignored) {}
                }
                return true;
            } else if ("ADMINISTRATOR".equalsIgnoreCase(role)) {
                return true;
            }
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Access Denied: Faculty credentials required.\",\"data\":null}");
            return false;
        }

        // Administrator endpoint protection
        if (uri.startsWith("/api/admin")) {
            if (!"ADMINISTRATOR".equalsIgnoreCase(role)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"success\":false,\"message\":\"Access Denied: Administrator privileges required.\",\"data\":null}");
                return false;
            }
            return true;
        }

        return true;
    }
}
