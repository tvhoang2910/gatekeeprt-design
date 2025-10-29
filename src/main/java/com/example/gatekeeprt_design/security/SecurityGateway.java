package com.example.gatekeeprt_design.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * GATEKEEPER PATTERN: Central validation and sanitization gateway
 * Uncomment @Component to enable security features
 */
// @Component
public class SecurityGateway {

    @Autowired
    private InputValidator validator;

    // ===== 1. SQL Injection Protection =====
    public boolean validateLogin(String username, String password) {
        // Parameterized query simulation
        if (!isValidUsername(username)) {
            return false;
        }
        // In real app, use PreparedStatement
        return authenticateUser(username, password);
    }

    private boolean authenticateUser(String username, String password) {
        // Simulate database check with safe parameters
        // In reality: PreparedStatement ps = conn.prepareStatement("SELECT * FROM users
        // WHERE username = ? AND password = ?");
        // ps.setString(1, username);
        // ps.setString(2, password);
        return username.equals("admin") && password.equals("admin123");
    }

    private boolean isValidUsername(String username) {
        return validator.isValidUsername(username);
    }

    // ===== 2. XSS Protection =====
    public String sanitizeXSS(String input) {
        if (input == null)
            return "";
        // Simple HTML escaping instead of OWASP library
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // ===== 3. Path Traversal Protection =====
    public String validateFilePath(String filename, String baseDir) throws SecurityException {
        if (!validator.isValidFilePath(filename)) {
            throw new SecurityException("Invalid file path: contains traversal attempts");
        }

        try {
            Path base = Paths.get(baseDir).toRealPath();
            Path requested = base.resolve(filename).toRealPath();

            if (!requested.startsWith(base)) {
                throw new SecurityException("Path traversal detected");
            }
            return requested.toString();
        } catch (Exception e) {
            throw new SecurityException("Invalid path: " + e.getMessage());
        }
    }

    // ===== 4. DoS Protection via Rate Limiting =====
    public boolean checkRateLimit(String clientId, int maxRequests, long timeWindowMs) {
        // This would use Redis or cache in production
        // For demo, return true (allow)
        return true;
    }

    // ===== 5. File Size Validation =====
    public boolean validateFileSize(long fileSize, long maxSize) {
        return fileSize > 0 && fileSize <= maxSize;
    }

    // ===== 6. Input Validation =====
    public boolean validateEmail(String email) {
        return validator.isValidEmail(email);
    }

    public boolean validatePassword(String password) {
        return validator.isValidPassword(password);
    }

    public boolean validateUsername(String username) {
        return validator.isValidUsername(username);
    }

    // ===== 7. Content Security Policy Helper =====
    public String addCSPHeaders() {
        return "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';";
    }
}
