package com.demo.weatherapi.controller;

import com.demo.weatherapi.model.Weather;
import com.demo.weatherapi.service.WeatherService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Weather weather) {
        weatherService.create(weather);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<Weather>> readAll() {
        final List<Weather> weathers = weatherService.readAll();

        return !weathers.isEmpty()
                ? new ResponseEntity<>(weathers, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Weather> read(@PathVariable(name = "id") int id) {
        final Weather weather = weatherService.read(id);

        return weather != null
                ? new ResponseEntity<>(weather, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable(name = "id") int id) {
        boolean deleted = weatherService.delete(id);
        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Weather> update(
            @RequestBody Weather weather, @PathVariable(name = "id") int id) {
        boolean updated = weatherService.update(weather, id);
        return updated
                ? ResponseEntity.ok(weather)
                : ResponseEntity.notFound().build();
    }

}