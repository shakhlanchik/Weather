package com.demo.weatherapi.controller;

import com.demo.weatherapi.model.Forecast;
import com.demo.weatherapi.service.ForecastService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forecast")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @PostMapping("/{cityId}")
    public ResponseEntity<?> create(@PathVariable int cityId, @RequestBody Forecast forecast) {
        forecastService.create(cityId, forecast); // Передаем ID из URL
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("{\"message\": \"Forecast created successfully\"}");
    }

    @GetMapping("/all")
    public ResponseEntity<List<Forecast>> readAll() {
        final List<Forecast> forecasts = forecastService.readAll();

        return forecasts != null &&  !forecasts.isEmpty()
                ? new ResponseEntity<>(forecasts, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{cityId}")
    public ResponseEntity<Forecast> read(@PathVariable(name = "cityId") int cityId) {
        final Forecast forecast = forecastService.read(cityId);

        return forecast != null
                ? new ResponseEntity<>(forecast, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}