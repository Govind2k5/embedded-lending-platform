package com.lendingplatform.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Customizes the title/description shown at the top of the auto-generated
 * Swagger UI (http://localhost:8080/swagger-ui/index.html). Everything else
 * about the OpenAPI spec - every endpoint, request/response schema - is
 * generated automatically by springdoc-openapi from the @RestController
 * classes and their DTOs, so it can never drift out of sync with the real
 * API the way a hand-written Postman collection or Markdown doc could.
 */
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
