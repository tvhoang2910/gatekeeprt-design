package com.example.gatekeeprt_design.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

//@Component
@Order(1)
public class BenchmarkFilter implements Filter {

    /**
     * Metrics container
     */
    private static class RequestMetrics {
        AtomicLong totalRequests = new AtomicLong(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        AtomicLong successfulRequests = new AtomicLong(0);
        AtomicLong failedRequests = new AtomicLong(0);
        Map<Integer, AtomicLong> statusCodeCount = new ConcurrentHashMap<>();
        Map<String, Long> endpointMetrics = new ConcurrentHashMap<>();
    }

    private static final RequestMetrics metrics = new RequestMetrics();

    @Override
    public void doFilter(final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();
        String endpoint = httpRequest.getRequestURI();

        try {
            chain.doFilter(request, response);
        } finally {
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            recordMetrics(httpResponse, endpoint, responseTime);
        }
    }

    private void recordMetrics(final HttpServletResponse response,
            final String endpoint,
            final long responseTime) {

        metrics.totalRequests.incrementAndGet();
        metrics.totalResponseTime.addAndGet(responseTime);

        int statusCode = response.getStatus();
        metrics.statusCodeCount
                .computeIfAbsent(statusCode, k -> new AtomicLong(0))
                .incrementAndGet();

        if (statusCode >= 200 && statusCode < 300) {
            metrics.successfulRequests.incrementAndGet();
        } else {
            metrics.failedRequests.incrementAndGet();
        }

        metrics.endpointMetrics.put(endpoint, responseTime);
    }

    /**
     * Get all metrics as a formatted string
     */
    public static String getMetrics() {
        long totalRequests = metrics.totalRequests.get();
        long avgResponseTime = totalRequests > 0 ? metrics.totalResponseTime.get() / totalRequests : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("=== BENCHMARK METRICS ===\n");
        sb.append("Total Requests: ").append(totalRequests).append("\n");
        sb.append("Successful Requests: ").append(metrics.successfulRequests.get()).append("\n");
        sb.append("Failed Requests: ").append(metrics.failedRequests.get()).append("\n");
        sb.append("Average Response Time: ").append(avgResponseTime).append("ms\n");
        sb.append("Total Response Time: ").append(metrics.totalResponseTime.get()).append("ms\n");
        sb.append("\nStatus Code Distribution:\n");

        metrics.statusCodeCount
                .forEach((code, count) -> sb.append("  ").append(code).append(": ").append(count.get()).append("\n"));

        return sb.toString();
    }

    /**
     * Get metrics as JSON format (for Grafana integration)
     */
    public static String getMetricsJSON() {
        long totalRequests = metrics.totalRequests.get();
        long avgResponseTime = totalRequests > 0 ? metrics.totalResponseTime.get() / totalRequests : 0;

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"totalRequests\": ").append(totalRequests).append(",\n");
        json.append("  \"successfulRequests\": ").append(metrics.successfulRequests.get()).append(",\n");
        json.append("  \"failedRequests\": ").append(metrics.failedRequests.get()).append(",\n");
        json.append("  \"averageResponseTime\": ").append(avgResponseTime).append(",\n");
        json.append("  \"totalResponseTime\": ").append(metrics.totalResponseTime.get()).append(",\n");
        json.append("  \"successRate\": ");

        if (totalRequests > 0) {
            double successRate = (metrics.successfulRequests.get() * 100.0) / totalRequests;
            json.append(String.format("%.2f", successRate));
        } else {
            json.append("0.00");
        }

        json.append(",\n");
        json.append("  \"statusCodeDistribution\": {\n");

        List<Map.Entry<Integer, AtomicLong>> entries = new ArrayList<>(
                metrics.statusCodeCount.entrySet());
        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<Integer, AtomicLong> entry = entries.get(i);
            json.append("    \"").append(entry.getKey()).append("\": ")
                    .append(entry.getValue().get());
            if (i < entries.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  }\n");
        json.append("}\n");

        return json.toString();
    }

    /**
     * Reset all metrics
     */
    public static void resetMetrics() {
        metrics.totalRequests.set(0);
        metrics.totalResponseTime.set(0);
        metrics.successfulRequests.set(0);
        metrics.failedRequests.set(0);
        metrics.statusCodeCount.clear();
        metrics.endpointMetrics.clear();
    }

    /**
     * Get total requests count
     */
    public static long getTotalRequests() {
        return metrics.totalRequests.get();
    }

    /**
     * Get average response time
     */
    public static long getAverageResponseTime() {
        long totalRequests = metrics.totalRequests.get();
        return totalRequests > 0 ? metrics.totalResponseTime.get() / totalRequests : 0;
    }

    /**
     * Get successful requests count
     */
    public static long getSuccessfulRequests() {
        return metrics.successfulRequests.get();
    }

    /**
     * Get failed requests count
     */
    public static long getFailedRequests() {
        return metrics.failedRequests.get();
    }

    /**
     * Get success rate as percentage
     */
    public static double getSuccessRate() {
        long totalRequests = metrics.totalRequests.get();
        return totalRequests > 0 ? (metrics.successfulRequests.get() * 100.0) / totalRequests : 0.0;
    }
}
