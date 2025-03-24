package com.demo.weatherapi.controller;

import com.demo.weatherapi.model.Weather;
import com.demo.weatherapi.service.WeatherService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @Autowired // Внедрение зависимости через конструктор
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Weather weather) {
        WeatherService.create(weather);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Weather>> readAll() {
        final List<Weather> weathers = WeatherService.readAll();

        return !weathers.isEmpty()
                ? new ResponseEntity<>(weathers, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{cityId}")
    public ResponseEntity<Weather> read(@PathVariable(name = "cityId") int cityId) {
        final Weather weather = weatherService.read(cityId);

        return weather != null
                ? new ResponseEntity<>(weather, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/search/by-condition")
    public ResponseEntity<List<Weather>> searchByCondition(@RequestParam String condition) {
        final List<Weather> results = weatherService.findByCondition(condition);

        return results.isEmpty()
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(results)
                : ResponseEntity.ok(results);
    }

}
