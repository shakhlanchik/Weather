package com.demo.weatherapi.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class Weather {
    private int cityId;
    private double temperature;
    private String condition;
    private double humidity;
    private double windSpeed;

    public Weather(int cityId, double temperature, String condition,
                   double humidity, double windSpeed) {
        this.cityId = cityId;
        this.temperature = temperature;
        this.condition = condition;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
    }

}
