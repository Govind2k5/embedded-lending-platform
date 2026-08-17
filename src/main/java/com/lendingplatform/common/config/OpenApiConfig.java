package com.lendingplatform.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lendingPlatformOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Embedded Lending Platform API")
                .description("Simplified embedded digital lending marketplace - educational project, all lenders and data are fictional.")
                .version("v1"));
    }
}
