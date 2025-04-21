package com.demo.weatherapi.service;

import com.demo.weatherapi.model.Weather;
import java.util.List;

public interface WeatherService {
    void create(Weather weather);

    List<Weather> readAll();

    Weather read(int cityId);

    boolean update(Weather weather, int cityId);

    boolean delete(int cityId);
}