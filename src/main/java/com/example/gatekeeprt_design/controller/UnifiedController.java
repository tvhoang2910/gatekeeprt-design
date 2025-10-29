package com.example.gatekeeprt_design.controller;

import com.example.gatekeeprt_design.security.InputValidator;
import com.example.gatekeeprt_design.security.SecurityGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping
public class UnifiedController {

    @Autowired(required = false)
    private SecurityGateway securityGateway; // Gatekeeper - may be null if disabled

    @Autowired(required = false)
    private InputValidator inputValidator;

    // Simple login page
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // Login handler - vulnerable to SQLi if Gatekeeper disabled
    @PostMapping("/login")
    public String login(@RequestParam String username,
            @RequestParam String password,
            Model model) {
        // ========== VULNERABLE: SQL Injection ==========
        boolean authenticated = loginWithSQLi(username, password);

        // ========== SECURE (UNCOMMENT / ENABLE GATEKEEPER): SQL Injection Protection
        // ==========
        // boolean authenticated = false;
        // if (securityGateway != null) {
        // authenticated = securityGateway.validateLogin(username, password);
        // } else {
        // authenticated = loginWithSQLi(username, password);
        // }

        if (authenticated) {
            model.addAttribute("message", "Welcome, " + username);
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Login failed!");
            return "login";
        }
    }

    // Simple simulated SQL login (vulnerable example)
    private boolean loginWithSQLi(String username, String password) {
        // Simulate a vulnerable SQL query built by concatenation (unsafe):
        // "SELECT u FROM users u WHERE username = '" + username + "' AND password = '"
        // + password + "'"
        // We'll represent the constructed SQL string and then simulate how a DB would
        // evaluate it
        String simulatedSql = "SELECT u FROM users u WHERE username = '" + username + "' AND password = '" + password
                + "'";

        // Normalize for simple pattern checks (lowercase)
        String lower = simulatedSql.toLowerCase();

        // If attacker injects an always-true condition using OR (common SQLi patterns),
        // treat as bypass
        if (lower.contains(" or 1=1") || lower.contains(" or 1==1") || lower.contains("' or '1'='1")
                || lower.contains("\" or \"1\"=\"1\"")) {
            return true; // simulated SQLi bypass
        }

        // Otherwise use a naive credential check (demo only)
        return "admin".equals(username) && "admin123".equals(password);
    }

    // Search endpoint - demonstrates XSS when output is not escaped
    @GetMapping("/search")
    public String searchPage() {
        return "search";
    }

    @PostMapping("/search")
    public String search(@RequestParam String query, Model model) {
        // ========== VULNERABLE: XSS ==========
        String result = query + " - Search result"; // no escaping

        // ========== SECURE (UNCOMMENT / ENABLE GATEKEEPER): XSS Protection ==========
        // if (securityGateway != null) {
        // result = securityGateway.sanitizeXSS(query) + " - Search result";
        // }

        model.addAttribute("result", result);
        return "search";
    }

    // View log - demonstrates path traversal when filename is concatenated
    @GetMapping("/view-log")
    public String viewLog(@RequestParam String filename, Model model) {
        // ========== VULNERABLE: Path Traversal ==========
        String filePath = "logs/" + filename;

        // ========== SECURE (UNCOMMENT / ENABLE GATEKEEPER): Path Traversal Protection
        // ==========
        // if (securityGateway != null) {
        // filePath = securityGateway.validateFilePath(filename, "logs");
        // } else {
        // filePath = "logs/" + filename;
        // }

        try {
            Path p = Paths.get(filePath).toAbsolutePath().normalize();
            if (!Files.exists(p) || Files.isDirectory(p)) {
                model.addAttribute("error", "File not found");
                return "view-log";
            }
            String content = Files.readString(p, StandardCharsets.UTF_8);
            model.addAttribute("content", content);
        } catch (IOException e) {
            model.addAttribute("error", "Unable to read file: " + e.getMessage());
        }
        return "view-log";
    }

    // Simple endpoint to show demo log (safe demo)
    @GetMapping("/view-log-demo")
    public String viewLogDemo(Model model) {
        Path p = Paths.get("logs/demo.log").toAbsolutePath().normalize();
        try {
            if (Files.exists(p)) {
                String content = Files.readString(p, StandardCharsets.UTF_8);
                model.addAttribute("content", content);
            } else {
                model.addAttribute("content", "(demo log not found)");
            }
        } catch (IOException e) {
            model.addAttribute("content", "Error: " + e.getMessage());
        }
        return "view-log";
    }

    // Upload endpoint - demonstrates file size checks
    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, Model model) {
        if (file == null || file.isEmpty()) {
            model.addAttribute("error", "File is empty");
            return "upload";
        }

        // ========== VULNERABLE: No proper server-side validation ==========
        long maxSize = Long.MAX_VALUE; // effectively no limit

        // ========== SECURE (UNCOMMENT / ENABLE GATEKEEPER): File Size Validation
        // ==========
        // long maxSize = 1 * 1024 * 1024; // 1 MB
        // if (securityGateway != null) {
        // boolean isValid = securityGateway.validateFileSize(file.getSize(), maxSize);
        // if (!isValid) {
        // model.addAttribute("error", "File upload failed: File size exceeds the
        // limit.");
        // return "upload";
        // }
        // }

        try {
            Path uploads = Paths.get("uploads").toAbsolutePath().normalize();
            Files.createDirectories(uploads);
            Path dest = uploads.resolve(Paths.get(file.getOriginalFilename())).normalize();
            Files.write(dest, file.getBytes());
            model.addAttribute("message", "File uploaded: " + file.getOriginalFilename());
        } catch (IOException e) {
            model.addAttribute("error", "Upload failed: " + e.getMessage());
        }
        return "upload";
    }

    // Register endpoint - demonstrates input validation bypass
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
            @RequestParam String password,
            @RequestParam String username,
            Model model) {
        // ========== VULNERABLE: No input validation ==========
        // Store as-is (demo only)

        // ========== SECURE (UNCOMMENT / ENABLE GATEKEEPER): Input Validation
        // ==========
        // if (securityGateway != null) {
        // if (!securityGateway.validateEmail(email) ||
        // !securityGateway.validatePassword(password) ||
        // !securityGateway.validateUsername(username)) {
        // model.addAttribute("error", "Invalid input format");
        // return "register";
        // }
        // }

        // Simulate successful registration
        model.addAttribute("message", "Registered user: " + username);
        return "register";
    }

    // Rate limit test endpoint
    @GetMapping("/rate-limit-test")
    public String rateLimitTest(Model model) {
        model.addAttribute("message", "OK");
        return "dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // simple dashboard
        return "dashboard";
    }

}
