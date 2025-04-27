package com.demo.weatherapi.repository;

import com.demo.weatherapi.model.Forecast;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForecastRepository extends JpaRepository<Forecast, Long> {
    @Query("SELECT f FROM Forecast f WHERE f.city.id = :cityId AND f.date = :date")
    List<Forecast> findForecastsByCityIdAndDate(
            @Param("cityId") Integer cityId, @Param("date") LocalDate date);
}