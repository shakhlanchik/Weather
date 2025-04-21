package com.demo.weatherapi.service;

import com.demo.weatherapi.model.Forecast;
import com.demo.weatherapi.repository.ForecastRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class ForecastServiceImpl implements ForecastService {

    private final ForecastRepository forecastRepository;

    @Autowired
    public ForecastServiceImpl(ForecastRepository forecastRepository) {
        this.forecastRepository = forecastRepository;
    }

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
}
