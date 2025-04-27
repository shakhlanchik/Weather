package com.demo.weatherapi.service;

import com.demo.weatherapi.cache.WeatherCache;
import com.demo.weatherapi.model.Weather;
import com.demo.weatherapi.repository.WeatherRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final WeatherRepository weatherRepository;
    private static final Logger logger = LoggerFactory.getLogger(WeatherServiceImpl.class);
    private final WeatherCache weatherCache;

    @Autowired
    public WeatherServiceImpl(WeatherRepository weatherRepository, WeatherCache weatherCache) {
        this.weatherRepository = weatherRepository;
        this.weatherCache = weatherCache;
    }

    @Override
    public void create(Weather weather) {
        weatherRepository.save(weather);
    }

    @Override
    public List<Weather> readAll() {
        return weatherRepository.findAll();
    }

    @Override
    public Weather read(int id) {
        Weather cachedWeather = weatherCache.get(id);
        if (cachedWeather != null) {
            logger.info("Данные получены из кэша для ключа: {}", id);
            return cachedWeather;
        }

        Optional<Weather> weather = weatherRepository.findById((long) id);
        if (weather.isPresent()) {
            logger.info("Данные загружены из БД и сохранены в кэш для ключа: {}", id);
            weatherCache.put(id, weather.get());
            return weather.get();
        }

        return null;
    }


    @Override
    public boolean update(Weather newWeather, int id) {
        Optional<Weather> optionalWeather = weatherRepository.findById((long) id);
        if (optionalWeather.isPresent()) {
            Weather existingWeather = optionalWeather.get();

            existingWeather.setTemperature(newWeather.getTemperature());
            existingWeather.setCondition(newWeather.getCondition());
            existingWeather.setHumidity(newWeather.getHumidity());
            existingWeather.setWindSpeed(newWeather.getWindSpeed());
            existingWeather.setCity(newWeather.getCity());

            weatherRepository.save(existingWeather);
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        if (weatherRepository.existsById((long) id)) {
            weatherRepository.deleteById((long) id);
            return true;
        }
        return false;
    }
}