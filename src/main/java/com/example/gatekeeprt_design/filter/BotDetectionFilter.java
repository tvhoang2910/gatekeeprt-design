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
import java.util.regex.Pattern;

//@Component
@Order(2)
public class BotDetectionFilter implements Filter {

    /**
     * HTTP status code returned when bot is detected.
     */
    private static final int BOT_DETECTED_STATUS = 403;

    /**
     * List of common bot User-Agent patterns
     */
    private static final List<Pattern> BOT_PATTERNS = Arrays.asList(
            Pattern.compile("bot", Pattern.CASE_INSENSITIVE),
            Pattern.compile("crawler", Pattern.CASE_INSENSITIVE),
            Pattern.compile("spider", Pattern.CASE_INSENSITIVE),
            Pattern.compile("scraper", Pattern.CASE_INSENSITIVE),
            Pattern.compile("scrapy", Pattern.CASE_INSENSITIVE),
            Pattern.compile("curl", Pattern.CASE_INSENSITIVE),
            Pattern.compile("wget", Pattern.CASE_INSENSITIVE),
            Pattern.compile("python", Pattern.CASE_INSENSITIVE),
            Pattern.compile("java(?!script)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("perl", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ruby", Pattern.CASE_INSENSITIVE),
            Pattern.compile("googlebot", Pattern.CASE_INSENSITIVE),
            Pattern.compile("bingbot", Pattern.CASE_INSENSITIVE),
            Pattern.compile("slurp", Pattern.CASE_INSENSITIVE),
            Pattern.compile("duckduckbot", Pattern.CASE_INSENSITIVE),
            Pattern.compile("baiduspider", Pattern.CASE_INSENSITIVE),
            Pattern.compile("yandexbot", Pattern.CASE_INSENSITIVE),
            Pattern.compile("facebookexternalhit", Pattern.CASE_INSENSITIVE),
            Pattern.compile("twitterbot", Pattern.CASE_INSENSITIVE));

    /**
     * Counter for detected bots
     */
    private static long botDetectionCount = 0;

    @Override
    public void doFilter(final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (isBot(httpRequest)) {
            botDetectionCount++;
            httpResponse.setStatus(BOT_DETECTED_STATUS);
            httpResponse.getWriter().write("Bot detected and blocked\n");
            return;
        }

        chain.doFilter(request, response);
    }

    /**
     * Check if the request is from a bot by analyzing User-Agent header
     */
    private boolean isBot(final HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");

        if (userAgent == null || userAgent.isEmpty()) {
            return true; // No User-Agent is suspicious
        }

        for (Pattern pattern : BOT_PATTERNS) {
            if (pattern.matcher(userAgent).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the total number of bots detected
     */
    public static long getBotDetectionCount() {
        return botDetectionCount;
    }

    /**
     * Reset the bot detection counter
     */
    public static void resetBotDetectionCount() {
        botDetectionCount = 0;
    }
}
