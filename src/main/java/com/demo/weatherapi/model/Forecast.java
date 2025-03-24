package com.demo.weatherapi.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class Forecast {
    private int cityId;
    private final List<DailyWeather> forecast;
    private List<DailyWeather> dailyForecast;

    public Forecast(int cityId, List<DailyWeather> tempForecast) {
        this.cityId = cityId;
        this.forecast = tempForecast;
    }

}
