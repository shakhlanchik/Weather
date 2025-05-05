package com.demo.weatherapi.cache;

import com.demo.weatherapi.model.Forecast;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ForecastCache {

    private static final int MAX_CACHE_SIZE = 20;
    private final Map<String, List<Forecast>> cache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<Forecast>> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    public List<Forecast> get(String key) {
        return cache.get(key);
    }

    public void put(String key, List<Forecast> forecasts) {
        cache.put(key, forecasts);
    }

    public List<String> getAllKeys() {
        return List.copyOf(cache.keySet());
    }
}