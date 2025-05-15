package com.demo.weatherapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Weather API",
                version = "1.0",
                description = "API для работы с прогнозами погоды и городами"
        )
)
public class OpenApiConfig {
}
