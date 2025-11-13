package com.example.gatekeeprt_design.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.example.gatekeeprt_design.filter.BotDetectionFilter;
import com.example.gatekeeprt_design.filter.BenchmarkFilter;
import com.example.gatekeeprt_design.filter.RateLimitingFilter;
import com.example.gatekeeprt_design.util.ApplicationContextProvider;
import org.springframework.context.ApplicationContext;

@Component
public class SecurityGateway {

    @Autowired(required = false)
    private InputValidator validator;

    private static ApplicationContext getContext() {
        return ApplicationContextProvider.getApplicationContext();
    }

    /**
     * Check if Rate Limiting filter is enabled
     */
    public static boolean isRateLimitingEnabled() {
        ApplicationContext ctx = getContext();
        return ctx != null && ctx.containsBeanDefinition("rateLimitingFilter")
                || ctx != null && ctx.getBeansOfType(RateLimitingFilter.class).size() > 0;
    }

    /**
     * Check if Bot Detection filter is enabled
     */
    public static boolean isBotDetectionEnabled() {
        ApplicationContext ctx = getContext();
        return ctx != null && ctx.containsBeanDefinition("botDetectionFilter")
                || ctx != null && ctx.getBeansOfType(BotDetectionFilter.class).size() > 0;
    }

    /**
     * Check if Benchmark filter is enabled
     */
    public static boolean isBenchmarkEnabled() {
        ApplicationContext ctx = getContext();
        return ctx != null && ctx.containsBeanDefinition("benchmarkFilter")
                || ctx != null && ctx.getBeansOfType(BenchmarkFilter.class).size() > 0;
    }

    /**
     * Get bot detection statistics
     */
    public static long getBotDetectionCount() {
        return BotDetectionFilter.getBotDetectionCount();
    }

    /**
     * Get benchmark metrics
     */
    public static String getBenchmarkMetrics() {
        return BenchmarkFilter.getMetrics();
    }

    /**
     * Get benchmark metrics as JSON
     */
    public static String getBenchmarkMetricsJSON() {
        return BenchmarkFilter.getMetricsJSON();
    }

    public boolean validateLogin(String username, String password) {
        if (!isValidUsername(username)) {
            return false;
        }
        return authenticateUser(username, password);
    }

    private boolean authenticateUser(String username, String password) {
        return username.equals("admin") && password.equals("admin123");
    }

    private boolean isValidUsername(String username) {
        return validator.isValidUsername(username);
    }

    public String sanitizeXSS(String input) {
        if (input == null)
            return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

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

    public boolean checkRateLimit(String clientId, int maxRequests, long timeWindowMs) {
        return true;
    }

    public boolean validateFileSize(long fileSize, long maxSize) {
        return fileSize > 0 && fileSize <= maxSize;
    }

    public boolean validateEmail(String email) {
        return validator.isValidEmail(email);
    }

    public boolean validatePassword(String password) {
        return validator.isValidPassword(password);
    }

    public boolean validateUsername(String username) {
        return validator.isValidUsername(username);
    }

    public String addCSPHeaders() {
        return "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';";
    }
}
