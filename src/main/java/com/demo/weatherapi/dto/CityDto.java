package com.demo.weatherapi.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Schema(description = "Город с краткой информацией и списком прогнозов")
public class CityDto {

    @Schema(
            description = "Идентификатор города",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Integer id;

    @Schema(
            description = "Название города",
            example = "Санкт-Петербург",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String name;

    @ArraySchema(
            schema = @Schema(implementation = ForecastDto.class),
            arraySchema = @Schema(
                    description = "Список прогнозов погоды для города",
                    nullable = true
            )
    )
    private List<ForecastDto> forecasts;
}