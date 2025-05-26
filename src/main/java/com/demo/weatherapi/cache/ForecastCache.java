package com.demo.weatherapi.cache;

import com.demo.weatherapi.dto.ForecastDto;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ForecastCache {
    private static final Logger log = LoggerFactory.getLogger(ForecastCache.class);
    private static final int MAX_CACHE_SIZE = 50;
    String cityName = "city:";
    String dateStr = ":date:";

    private final Map<Integer, ForecastDto> singleForecastCache = new ConcurrentHashMap<>();

    private final Map<String, List<ForecastDto>> listForecastCache = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<ForecastDto>> eldest) {
            boolean shouldRemove = size() > MAX_CACHE_SIZE;
            if (shouldRemove) {
                log.debug("Evicting oldest cache entry: {}", eldest.getKey());
            }
            return shouldRemove;
        }
    };

    public ForecastDto getForecastById(Integer id) {
        ForecastDto forecast = singleForecastCache.get(id);
        if (forecast != null) {
            log.debug("Cache hit for forecast {}", id);
        } else {
            log.debug("Cache miss for forecast {}", id);
        }
        return forecast;
    }

    public List<ForecastDto> getForecastsByCityId(Integer cityId) {
        String key = cityName + cityId;
        return listForecastCache.get(key);
    }

    public List<ForecastDto> getForecastsByNameAndDate(String name, LocalDate date) {
        String key = "name:" + name + dateStr + date;
        return listForecastCache.get(key);
    }

    public void cacheSingleForecast(ForecastDto forecast) {
        singleForecastCache.put(forecast.getId(), forecast);
        log.debug("Cached single forecast {}", forecast.getId());
    }

    public void cacheForecastsByCityId(Integer cityId, List<ForecastDto> forecasts) {
        String key = cityName + cityId;
        listForecastCache.put(key, forecasts);
        log.debug("Cached {} forecasts for specified city", forecasts.size());
    }

    public void cacheForecastsByNameAndDate(
            String name, LocalDate date, List<ForecastDto> forecasts) {
        String key = "name:" + name + dateStr + date;
        listForecastCache.put(key, forecasts);
        log.debug("Cached {} forecasts for city on specified date", forecasts.size());
    }

    public void evictForecastsByCity(Integer cityId) {
        listForecastCache.keySet().removeIf(key -> key.startsWith(cityName + cityId)
                || key.contains(":" + cityName + cityId + ":"));

        singleForecastCache.values().removeIf(f -> f.getCityId().equals(cityId));

        log.debug("Evicted all cache for city {}", cityId);
    }

    public void evictSingleForecast(Integer forecastId) {
        singleForecastCache.remove(forecastId);
        log.debug("Evicted forecast {}", forecastId);
    }

    public void evictForecastsByCityAndDate(Integer cityId, LocalDate date) {
        String keyPrefix = cityName + cityId + dateStr + date;
        listForecastCache.keySet().removeIf(key -> key.startsWith(keyPrefix));
        log.debug("Evicted cache for specified city and date");
    }

    public List<ForecastDto> getForecastsByName(String name, String country) {
        String key = "name:" + name + "country:" + country;
        return listForecastCache.get(key);
    }

    public void cacheForecastsByName(String name, String country, List<ForecastDto> forecasts) {
        String key = "name:" + name + "country:" + country;
        listForecastCache.put(key, forecasts);
        log.debug("Cached {} forecasts for city", forecasts.size());
    }
}