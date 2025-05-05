package com.demo.weatherapi.controller;

import com.demo.weatherapi.cache.ForecastCache;
import com.demo.weatherapi.model.Forecast;
import com.demo.weatherapi.service.ForecastService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forecast")
public class ForecastController {

    private final ForecastService forecastService;
    private ForecastCache forecastCache;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Forecast forecast) {
        forecastService.create(forecast);
        return new ResponseEntity<>(
                "{\"message\": \"Forecast created successfully\"}",
                HttpStatus.CREATED
        );
    }

    @GetMapping("/all")
    public ResponseEntity<List<Forecast>> readAll() {
        List<Forecast> forecasts = forecastService.readAll();
        if (forecasts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(forecasts, HttpStatus.OK);
    }

    @GetMapping("/{cityId}")
    public ResponseEntity<Forecast> read(@PathVariable int cityId) {
        Forecast forecast = forecastService.read(cityId);
        return forecast != null
                ? new ResponseEntity<>(forecast, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/{cityId}")
    public ResponseEntity<String> update(@PathVariable int cityId, @RequestBody Forecast forecast) {
        boolean updated = forecastService.update(forecast, cityId);
        return updated
                ? ResponseEntity.ok("{\"message\": \"Forecast updated successfully\"}")
                : new ResponseEntity<>("{\"error\": \"Forecast not found\"}", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{cityId}")
    public ResponseEntity<String> delete(@PathVariable int cityId) {
        boolean deleted = forecastService.delete(cityId);
        return deleted
                ? ResponseEntity.ok("{\"message\": \"Forecast deleted successfully\"}")
                : new ResponseEntity<>("{\"error\": \"Forecast not found\"}", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Forecast>> getForecastsByCityAndDate(
            @RequestParam Integer cityId,
            @RequestParam String date) {

        LocalDate parsedDate = LocalDate.parse(date); // формат YYYY-MM-DD
        List<Forecast> forecasts = forecastService.getForecastsByCityIdAndDate(cityId, parsedDate);
        return ResponseEntity.ok(forecasts);
    }

    @GetMapping("/by-city/{cityId}")
    public ResponseEntity<List<Forecast>> getForecastsByCityId(@PathVariable int cityId) {
        List<Forecast> forecasts = forecastService.getForecastsByCityId(cityId);
        return forecasts != null && !forecasts.isEmpty()
                ? new ResponseEntity<>(forecasts, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/cache/status")
    public ResponseEntity<?> getCacheStatus() {
        return ResponseEntity.ok(forecastCache.getAllKeys());
    }
}