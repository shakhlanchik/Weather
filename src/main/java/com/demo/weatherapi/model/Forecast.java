package com.demo.weatherapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
@Entity
@Table(name = "forecasts")
@Schema(description = "Прогноз погоды на определённую дату в конкретном городе")
public class Forecast {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forecastIdSeq")
    @SequenceGenerator(name = "forecastIdSeq", sequenceName = "forecast_id_seq", allocationSize = 1)
    @Schema(description = "Идентификатор прогноза", example = "42",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @NotNull(message = "Поле 'city' обязательно")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id", nullable = false)
    @JsonBackReference
    @Schema(description = "Город, к которому относится прогноз", required = true)
    private City city;

    @NotNull(message = "Поле 'date' обязательно")
    @Schema(description = "Дата прогноза", example = "2025-05-11", required = true)
    private LocalDate date;

    @NotNull(message = "Минимальная температура обязательна")
    @Column(name = "temperature_min")
    @Schema(description = "Минимальная температура (°C)", example = "-5.2", required = true)
    private Double temperatureMin;

    @NotNull(message = "Максимальная температура обязательна")
    @Column(name = "temperature_max")
    @Schema(description = "Максимальная температура (°C)", example = "3.8", required = true)
    private Double temperatureMax;

    @Size(max = 255, message = "Описание погодных условий не должно превышать 255 символов")
    @Column(name = "condition")
    @Schema(description = "Описание погодных условий", example = "Пасмурно, возможен снег")
    private String condition;

}