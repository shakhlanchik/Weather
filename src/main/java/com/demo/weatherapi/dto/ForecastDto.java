package com.demo.weatherapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@Schema(description = "Прогноз погоды на конкретную дату")
public class ForecastDto {

    @Schema(
            description = "Идентификатор прогноза",
            example = "101",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private Integer id;

    @Schema(
            description = "ID города, к которому относится прогноз",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Integer cityId;

    @Schema(
            description = "Дата прогноза",
            example = "2025-05-11",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate date;

    @Schema(
            description = "Минимальная температура (°C)",
            example = "-3.5",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double temperatureMin;

    @Schema(
            description = "Максимальная температура (°C)",
            example = "5.0",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Double temperatureMax;

    @Schema(
            description = "Описание погодных условий",
            example = "Облачно с прояснениями",
            nullable = true
    )
    private String condition;

    @Schema(
            description = "Относительная влажность воздуха",
            example = "75%",
            nullable = true
    )
    private Double humidity;

    @Schema(
            description = "Скорость ветра",
            example = "4.2 км/ч",
            nullable = true
    )
    private Double windSpeed;

    public ForecastDto(Integer id, Integer cityId, LocalDate date, double minTemp, double maxTemp, double humidity, double windSpeed) {
        this.id = id;
        this.cityId = cityId;
        this.date = date;
        this.temperatureMin = minTemp;
        this.temperatureMax = maxTemp;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
    }

    public ForecastDto() {

    }
}