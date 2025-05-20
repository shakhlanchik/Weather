package com.demo.weatherapi.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Стандартный ответ об ошибке")
public class ErrorResponse {

    @Schema(
            description = "Сообщение об ошибке",
            example = "Город не найден",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String message;

    @Schema(
            description = "HTTP статус код",
            example = "404",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String statusCode;

    @ArraySchema(
            schema = @Schema(implementation = String.class),
            arraySchema = @Schema(
                    description = "Список ошибок валидации",
                    nullable = true
            )
    )
    private final List<String> validationErrors;

    public ErrorResponse(String message, String statusCode) {
        this.message = message;
        this.statusCode = statusCode;
        this.validationErrors = null;
    }

    public ErrorResponse(String message, List<String> validationErrors) {
        this.message = message;
        this.statusCode = statusCode;
        this.validationErrors = validationErrors;
    }
}