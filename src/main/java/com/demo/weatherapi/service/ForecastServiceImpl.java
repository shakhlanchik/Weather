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


    @Override
    public void create(Forecast forecast) {
        forecastRepository.save(forecast);
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
    public boolean update(Forecast forecast, int cityId) {
        if (forecastRepository.existsById((long) cityId)) {
            forecast.setId(cityId);
            forecastRepository.save(forecast);
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(int cityId) {
        if (forecastRepository.existsById((long) cityId)) {
            forecastRepository.deleteById((long) cityId);
            return true;
        }
        return false;
    }

    public ForecastServiceImpl(ForecastRepository forecastRepository, ForecastCache forecastCache) {
        this.forecastRepository = forecastRepository;
        this.forecastCache = forecastCache;
    }

    public List<Forecast> getForecastsByCityIdAndDate(Integer cityId, LocalDate date) {
        String key = cityId + "_" + date;
        List<Forecast> forecasts = Optional.ofNullable(forecastCache.get(key)).orElseGet(() -> {
            List<Forecast> loadedForecasts = forecastRepository
                    .findForecastsByCityIdAndDate(cityId, date);
            logger.info("Данные загружены из БД и сохранены в кэш для ключа: {}", key);
            forecastCache.put(key, loadedForecasts);
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