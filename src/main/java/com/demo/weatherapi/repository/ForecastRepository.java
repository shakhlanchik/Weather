package com.demo.weatherapi.repository;

import com.demo.weatherapi.model.Forecast;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ForecastRepository extends JpaRepository<Forecast, Long> {
    @Query("SELECT f FROM Forecast f WHERE f.city.name = :name AND f.date = :date")
    List<Forecast> findForecastsByNameAndDate(
            @Param("name") String name, @Param("date") LocalDate date);

    @Query("SELECT f FROM Forecast f WHERE f.city.id = :cityId")
    List<Forecast> findByCityId(@Param("cityId") Integer cityId);
}