package com.demo.weatherapi.repository;

import com.demo.weatherapi.model.Weather;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    //магия
}