package com.demo.weatherapi.cache;

import com.demo.weatherapi.model.Weather;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class WeatherCache {
    private final Map<Integer, Weather> cache = new ConcurrentHashMap<>();

    public Weather get(Integer id) {
        return cache.get(id);
    }

    public void put(Integer id, Weather weather) {
        cache.put(id, weather);
    }
}