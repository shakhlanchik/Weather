package com.demo.weatherapi.repository;

import com.demo.weatherapi.model.Forecast;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForecastRepository extends JpaRepository<Forecast, Long> {
    //магия
}
