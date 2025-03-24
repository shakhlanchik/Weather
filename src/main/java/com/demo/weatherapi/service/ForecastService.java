package com.demo.weatherapi.service;

import com.demo.weatherapi.model.Forecast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class ForecastService {

    private static final Map<Integer, Forecast> FORECAST_REPOSITORY_MAP = new HashMap<>();

    private static final AtomicInteger FORECAST_ID_HOLDER = new AtomicInteger();

    public void create(int cityId, Forecast forecast) {
        if (FORECAST_REPOSITORY_MAP.containsKey(cityId)) {
            throw new IllegalArgumentException("City ID already exists!"); // Проверка на дубликаты
        }
        forecast.setCityId(cityId);
        FORECAST_REPOSITORY_MAP.put(cityId, forecast);
        System.out.printf("Saved forecast for cityID: %d%n", cityId);
    }

    public List<Forecast> readAll() {
        return new ArrayList<>(FORECAST_REPOSITORY_MAP.values());
    }

    public Forecast read(int cityId) {
        return FORECAST_REPOSITORY_MAP.get(cityId);
    }

    public boolean update(Forecast forecast, int cityId) {
        if (FORECAST_REPOSITORY_MAP.containsKey(cityId)) {
            forecast.setCityId(cityId);
            FORECAST_REPOSITORY_MAP.put(cityId, forecast);
            return true;
        }

        return false;
    }

    public boolean delete(int cityId) {
        return FORECAST_REPOSITORY_MAP.remove(cityId) != null;
    }

}