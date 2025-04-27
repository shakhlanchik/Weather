package com.demo.weatherapi.service;

import com.demo.weatherapi.model.Forecast;
import java.time.LocalDate;
import java.util.List;

public interface ForecastService {
    void create(Forecast forecast);

    List<Forecast> readAll();

    Forecast read(int cityId);

    boolean update(Forecast forecast, int cityId);

    boolean delete(int cityId);

    List<Forecast> getForecastsByCityIdAndDate(Integer cityId, LocalDate date);
}