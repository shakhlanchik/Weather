package com.demo.weatherapi.repository;

import com.demo.weatherapi.model.Weather;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherRepository extends JpaRepository<Weather, Long> {
    List<Weather> findByConditionContainingIgnoreCase(String condition);
    //магия
}