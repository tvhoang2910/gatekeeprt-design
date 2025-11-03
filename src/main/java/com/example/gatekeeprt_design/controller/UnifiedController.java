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
import org.springframework.web.bind.annotation.ResponseBody;
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
        boolean authenticated;
        if (securityGateway != null) {
            authenticated = securityGateway.validateLogin(username, password);
        } else {
            authenticated = unsafeLogin(username, password);
        }

        if (authenticated) {
            model.addAttribute("message", "Welcome, " + username);
            return "redirect:/dashboard";
        } else {
            model.addAttribute("error", "Login failed!");
            return "login";
        }
    }

    private boolean unsafeLogin(String username, String password) {
        String simulatedSql = "SELECT u FROM users u WHERE username = '" + username
                + "' AND password = '" + password + "'";
        String lower = simulatedSql.toLowerCase();
        if (lower.contains(" or 1=1") || lower.contains(" or 1==1")
                || lower.contains("' or '1'='1") || lower.contains("\" or \"1\"=\"1\"")) {
            return true;
        }
        return "admin".equals(username) && "admin123".equals(password);
    }

    // Search endpoint - demonstrates XSS when output is not escaped
    @GetMapping("/search")
    public String searchPage() {
        return "search";
    }

    @PostMapping("/search")
    public String search(@RequestParam String query, Model model) {
        String result = query + " - Search result";
        if (securityGateway != null) {
            result = securityGateway.sanitizeXSS(query) + " - Search result";
        } else {
            // Vulnerable: no escaping
        }

        model.addAttribute("result", result);
        return "search";
    }

    // View log - demonstrates path traversal when filename is concatenated
    @GetMapping("/view-log")
    public String viewLog(@RequestParam String filename, Model model) {
        String filePath = "logs/" + filename;
        if (securityGateway != null) {
            filePath = securityGateway.validateFilePath(filename, "logs");
        } else {
            // Vulnerable: direct path concatenation
        }

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

        if (securityGateway != null) {
            long maxSize = 100 * 1024 * 1024;
            if (!securityGateway.validateFileSize(file.getSize(), maxSize)) {
                model.addAttribute("error", "File size exceeds 1MB limit");
                return "upload";
            }
        } else {
            // Vulnerable: no server-side size check
        }

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
        if (inputValidator != null && !inputValidator.isValidEmail(email)) {
            model.addAttribute("error", "Invalid email");
            return "register";
        }
        if (securityGateway != null) {
            if (!securityGateway.validateEmail(email) ||
                    !securityGateway.validatePassword(password) ||
                    !securityGateway.validateUsername(username)) {
                model.addAttribute("error", "Invalid input format");
                return "register";
            }
        } else {
            // Vulnerable: no input validation
        }

        model.addAttribute("message", "Registered user: " + username);
        return "register";
    }

    // Rate limit test endpoint
    @GetMapping("/rate-limit-test")
    @ResponseBody
    public String rateLimitTest() {
        return "OK\n";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // simple dashboard
        return "dashboard";
    }

}
