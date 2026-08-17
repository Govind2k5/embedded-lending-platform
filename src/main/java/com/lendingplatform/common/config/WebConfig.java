package com.lendingplatform.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendingplatform.common.ratelimit.RateLimitInterceptor;
import com.lendingplatform.common.ratelimit.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(rateLimiterService, objectMapper))
                .addPathPatterns("/api/v1/**");
    }
}
