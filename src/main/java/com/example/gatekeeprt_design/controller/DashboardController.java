package com.example.gatekeeprt_design.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.ui.Model;
import com.example.gatekeeprt_design.filter.BotDetectionFilter;
import com.example.gatekeeprt_design.filter.BenchmarkFilter;
import com.example.gatekeeprt_design.security.SecurityGateway;

import java.util.HashMap;
import java.util.Map;

@Controller
public class DashboardController {

    /**
     * Display the dashboard page with current metrics
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("rateLimitingEnabled", SecurityGateway.isRateLimitingEnabled());
        model.addAttribute("botDetectionEnabled", SecurityGateway.isBotDetectionEnabled());
        model.addAttribute("benchmarkEnabled", SecurityGateway.isBenchmarkEnabled());

        model.addAttribute("botDetectionCount", BotDetectionFilter.getBotDetectionCount());
        model.addAttribute("totalRequests", BenchmarkFilter.getTotalRequests());
        model.addAttribute("averageResponseTime", BenchmarkFilter.getAverageResponseTime());

        return "dashboard";
    }

    /**
     * Get benchmark metrics as JSON (for AJAX)
     */
    @GetMapping("/api/metrics")
    @ResponseBody
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Get benchmark metrics
        long totalRequests = BenchmarkFilter.getTotalRequests();
        long successfulRequests = BenchmarkFilter.getSuccessfulRequests();
        long failedRequests = BenchmarkFilter.getFailedRequests();
        long averageResponseTime = BenchmarkFilter.getAverageResponseTime();
        double successRate = BenchmarkFilter.getSuccessRate();

        metrics.put("totalRequests", totalRequests);
        metrics.put("successfulRequests", successfulRequests);
        metrics.put("failedRequests", failedRequests);
        metrics.put("averageResponseTime", averageResponseTime);
        metrics.put("successRate", successRate);

        // Add bot detection count
        metrics.put("botsDetected", BotDetectionFilter.getBotDetectionCount());

        return metrics;
    }

    /**
     * Get bot detection count
     */
    @GetMapping("/api/bot-count")
    @ResponseBody
    public String getBotCount() {
        return "{\"botDetectionCount\": " + BotDetectionFilter.getBotDetectionCount() + "}";
    }

    /**
     * Reset bot detection counter
     */
    @PostMapping("/api/reset-bot-count")
    @ResponseBody
    public String resetBotCount() {
        BotDetectionFilter.resetBotDetectionCount();
        return "{\"status\": \"success\", \"message\": \"Bot detection count reset\"}";
    }

    /**
     * Reset benchmark metrics
     */
    @PostMapping("/api/reset-metrics")
    @ResponseBody
    public String resetMetrics() {
        BenchmarkFilter.resetMetrics();
        BotDetectionFilter.resetBotDetectionCount();
        return "{\"status\": \"success\", \"message\": \"All metrics reset\"}";
    }

    /**
     * Get plain text metrics (for manual viewing)
     */
    @GetMapping("/api/metrics/text")
    @ResponseBody
    public String getMetricsText() {
        return BenchmarkFilter.getMetrics();
    }
}
