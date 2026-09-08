package com.lendingplatform.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendingplatform.common.ratelimit.RateLimitInterceptor;
import com.lendingplatform.common.ratelimit.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers RateLimitInterceptor so it actually runs on incoming requests -
 * an interceptor class alone does nothing until it's added to Spring MVC's
 * interceptor chain here.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Applies to every endpoint under /api/v1/ - Swagger UI and the
        // OpenAPI JSON docs are served from different paths and are
        // deliberately not rate-limited.
        registry.addInterceptor(new RateLimitInterceptor(rateLimiterService, objectMapper))
                .addPathPatterns("/api/v1/**");
    }
}
