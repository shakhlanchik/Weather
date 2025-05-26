package com.demo.weatherapi.service;

import com.demo.weatherapi.dto.ForecastDto;
import java.time.LocalDate;
import java.util.List;

public interface ForecastService {

    ForecastDto create(ForecastDto forecastDto);

    List<ForecastDto> readAll();

    ForecastDto read(Integer forecastId);

    ForecastDto update(ForecastDto forecastDto, Integer forecastId);

    void delete(Integer forecastId);

    List<ForecastDto> getForecastsByNameAndDate(String name, LocalDate date);

    List<ForecastDto> getForecastsByName(String name, String country);

    List<ForecastDto> getForecastsByCityId(Integer cityId);

    List<ForecastDto> findByFilters(
            String cityName, LocalDate date, Double minTemp, Double maxTemp);

    List<ForecastDto> createBulk(List<ForecastDto> forecasts);

    List<ForecastDto> updateBulk(List<ForecastDto> forecasts);

    void deleteBulk(List<Integer> ids);
}