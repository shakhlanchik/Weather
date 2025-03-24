package com.demo.weatherapi.model;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class DailyWeather {
    private LocalDate now;
    private double temperatureMin;
    private double temperatureMax;
    private String condition;

    public DailyWeather(LocalDate now, double temperatureMin,
                        double temperatureMax, String condition) {
        this.now = now;
        this.temperatureMin = temperatureMin;
        this.temperatureMax = temperatureMax;
        this.condition = condition;
    }
}
