package com.lendingplatform.common.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendingplatform.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.time.Instant;

/**
 * General-purpose 60 requests/minute per IP limit applied to every API call.
 * Endpoint-specific limits (e.g. application creation per borrower) are
 * enforced separately in the relevant controller.
 */
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int LIMIT_PER_MINUTE = 60;

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = request.getRemoteAddr();
        String key = rateLimiterService.windowedKey("ratelimit:ip:" + clientIp, 60);

        if (!rateLimiterService.isAllowed(key, LIMIT_PER_MINUTE, Duration.ofMinutes(2))) {
            ErrorResponse body = new ErrorResponse(
                    Instant.now(), HttpStatus.TOO_MANY_REQUESTS.value(), "RATE_LIMIT_EXCEEDED",
                    "Too many requests, limit is " + LIMIT_PER_MINUTE + " per minute", request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(body));
            return false;
        }
        return true;
    }
}
