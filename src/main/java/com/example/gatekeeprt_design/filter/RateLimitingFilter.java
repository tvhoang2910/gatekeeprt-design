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
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

//@Component
public class RateLimitingFilter implements Filter {

    /**
     * Maximum number of requests allowed per client within the time window.
     */
    private static final int MAX_REQUESTS = 100;

    /**
     * Time window in milliseconds for rate limiting.
     */
    private static final long TIME_WINDOW = 60_000L;

    /**
     * HTTP status code returned when rate limit is exceeded.
     */
    private static final int TOO_MANY_REQUESTS = 429;

    /**
     * Map that stores timestamps of recent requests per client.
     */
    private final Map<String, Queue<Long>> requestMap = new ConcurrentHashMap<>();

    @Override
    public final void doFilter(final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (!isAllowed(getClientId(httpRequest))) {
            httpResponse.setStatus(TOO_MANY_REQUESTS);
            httpResponse.getWriter().write("Rate limit exceeded\n");
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientId(final HttpServletRequest request) {
        String clientId = request.getHeader("X-Forwarded-For");
        if (clientId == null || clientId.isEmpty()) {
            clientId = request.getRemoteAddr();
        }
        return clientId;
    }

    private boolean isAllowed(final String clientId) {
        long now = System.currentTimeMillis();
        Queue<Long> requests = requestMap.computeIfAbsent(
                clientId, k -> new LinkedList<>());

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
