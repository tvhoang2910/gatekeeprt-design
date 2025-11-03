package com.example.gatekeeprt_design.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {

    private static final int MAX_REQUESTS = 100;
    private static final long TIME_WINDOW = 60000;
    private final Map<String, Queue<Long>> requestMap = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientId = getClientId(httpRequest);
        if (!isAllowed(clientId)) {
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("Rate limit exceeded\n");
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientId(HttpServletRequest request) {
        String clientId = request.getHeader("X-Forwarded-For");
        if (clientId == null || clientId.isEmpty()) {
            clientId = request.getRemoteAddr();
        }
        return clientId;
    }

    private boolean isAllowed(String clientId) {
        long now = System.currentTimeMillis();
        Queue<Long> requests = requestMap.computeIfAbsent(clientId, k -> new LinkedList<>());

        while (!requests.isEmpty() && requests.peek() < now - TIME_WINDOW) {
            requests.poll();
        }

        if (requests.size() < MAX_REQUESTS) {
            requests.offer(now);
            return true;
        }
        return false;
    }
}
