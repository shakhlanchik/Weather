package com.demo.weatherapi.service;

import com.demo.weatherapi.cache.ForecastCache;
import com.demo.weatherapi.model.Forecast;
import com.demo.weatherapi.repository.ForecastRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service

public class ForecastServiceImpl implements ForecastService {

    private static final Logger logger = LoggerFactory.getLogger(ForecastServiceImpl.class);

    private final ForecastRepository forecastRepository;
    private final ForecastCache forecastCache;

    public ForecastServiceImpl(ForecastRepository forecastRepository, ForecastCache forecastCache) {
        this.forecastRepository = forecastRepository;
        this.forecastCache = forecastCache;
    }

    private void updateCacheIfChanged(String cityName, LocalDate date) {
        String key = cityName + "_" + date;
        List<Forecast> newForecasts = forecastRepository.findForecastsByNameAndDate(cityName, date);
        List<Forecast> cachedForecasts = forecastCache.get(key);

        if (!newForecasts.equals(cachedForecasts)) {
            forecastCache.put(key, newForecasts);
            logger.info("Кэш обновлён для ключа {} после изменений в БД", key);
        } else {
            logger.info("Кэш для ключа {} уже актуален", key);
        }
    }

    @Override
    public void create(Forecast forecast) {
        forecastRepository.save(forecast);

        updateCacheIfChanged(forecast.getCity().getName(), forecast.getDate());
    }

    @Override
    public boolean update(Forecast forecast, int forecastId) {
        if (forecastRepository.existsById((long) forecastId)) {
            forecast.setId(forecastId);
            forecastRepository.save(forecast);

            updateCacheIfChanged(forecast.getCity().getName(), forecast.getDate());

            logger.info("Прогноз с id {} обновлён", forecastId);
            return true;
        }
        return false;
    }

    @Override
    public List<Forecast> readAll() {
        return forecastRepository.findAll();
    }

    @Override
    public Forecast read(int cityId) {
        Optional<Forecast> forecast = forecastRepository.findById((long) cityId);
        return forecast.orElse(null);
    }

    @Override
    public boolean delete(int forecastId) {
        Optional<Forecast> optionalForecast = forecastRepository.findById((long) forecastId);
        if (optionalForecast.isPresent()) {
            Forecast forecast = optionalForecast.get();

            forecastRepository.deleteById((long) forecastId);

            String cacheKey = forecast.getCity().getName() + "_" + forecast.getDate();
            forecastCache.remove(cacheKey);

            logger.info("Прогноз с id {} удалён из БД и кэша для ключа: {}", forecastId, cacheKey);
            return true;
        }
        return false;
    }

    public List<Forecast> getForecastsByNameAndDate(String name, LocalDate date) {
        String key = name + "_" + date;
        List<Forecast> forecasts = Optional.ofNullable(forecastCache.get(key)).orElseGet(() -> {
            List<Forecast> loadedForecasts = forecastRepository
                    .findForecastsByNameAndDate(name, date);
            forecastCache.put(key, loadedForecasts);
            logger.info("Данные загружены из БД и сохранены в кэш для ключа: {}", key);
            return loadedForecasts;
        });

        if (forecasts == forecastCache.get(key)) {
            logger.info("Данные получены из кэша для ключа: {}", key);
        }
        return forecasts;
    }

    public List<Forecast> getForecastsByCityId(Integer cityId) {
        return forecastRepository.findByCityId(cityId);
    }
}
