//package com.demo.weatherapi.dto;
//
//import jakarta.validation.constraints.DecimalMax;
//import jakarta.validation.constraints.DecimalMin;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import jakarta.validation.constraints.Positive;
//import java.time.LocalDate;
//import lombok.Getter;
//import lombok.Setter;
//
//@Getter
//@Setter
//public class ForecastDto {
//
//    @NotNull(message = "ID города обязателен")
//    private Integer cityId;
//
//    @NotNull(message = "Дата прогноза обязательна")
//    private LocalDate date;
//
//    @NotNull(message = "Минимальная температура обязательна")
//    private Double temperatureMin;
//
//    @NotNull(message = "Максимальная температура обязательна")
//    private Double temperatureMax;
//
//    @NotBlank(message = "Погодное состояние обязательно")
//    private String condition;
//
//    @NotNull(message = "Влажность обязательна")
//    @DecimalMin(value = "0.0", message = "Влажность не может быть меньше 0")
//    @DecimalMax(value = "100.0", message = "Влажность не может быть больше 100")
//    private Double humidity;
//
//    @NotNull(message = "Скорость ветра обязательна")
//    @Positive(message = "Скорость ветра должна быть положительной")
//    private Double windSpeed;
//}
