package com.example.gatekeeprt_design.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.example.gatekeeprt_design.security.SecurityGateway;
import com.example.gatekeeprt_design.filter.BotDetectionFilter;
import com.example.gatekeeprt_design.filter.BenchmarkFilter;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/monitoring-dashboard")
    public String monitoringDashboard(Model model) {
        // Ensure the monitoring page has the same model attributes as /dashboard
        model.addAttribute("rateLimitingEnabled", SecurityGateway.isRateLimitingEnabled());
        model.addAttribute("botDetectionEnabled", SecurityGateway.isBotDetectionEnabled());
        model.addAttribute("benchmarkEnabled", SecurityGateway.isBenchmarkEnabled());

        model.addAttribute("botDetectionCount", BotDetectionFilter.getBotDetectionCount());
        model.addAttribute("totalRequests", BenchmarkFilter.getTotalRequests());
        model.addAttribute("averageResponseTime", BenchmarkFilter.getAverageResponseTime());

        return "monitoring-dashboard";
    }
}
