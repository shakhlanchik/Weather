package com.demo.weatherapi.service;

import com.demo.weatherapi.model.Weather;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {
    private static final Map<Integer, Weather> WEATHER_REPOSITORY_MAP = new HashMap<>();

    private static final AtomicInteger WEATHER_ID_HOLDER = new AtomicInteger();

    public static void create(Weather weather) {
        final int weatherCity = WEATHER_ID_HOLDER.incrementAndGet();
        weather.setCityId(weatherCity);
        WEATHER_REPOSITORY_MAP.put(weatherCity, weather);
    }

    public static List<Weather> readAll() {
        return new ArrayList<>(WEATHER_REPOSITORY_MAP.values());
    }

    public Weather read(int id) {
        return WEATHER_REPOSITORY_MAP.get(id);
    }

    public boolean update(Weather weather, int id) {
        if (WEATHER_REPOSITORY_MAP.containsKey(id)) {
            weather.setCityId(id);
            WEATHER_REPOSITORY_MAP.put(id, weather);
            return true;
        }

        return false;
    }

    public boolean delete(int id) {
        return WEATHER_REPOSITORY_MAP.remove(id) != null;
    }
}