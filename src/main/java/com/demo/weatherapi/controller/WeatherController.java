package com.demo.weatherapi.controller;

import com.demo.weatherapi.model.Weather;
import com.demo.weatherapi.service.WeatherService;
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
@RequestMapping("/weather")
public class WeatherController {

    private WeatherService weatherService;

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

    @GetMapping("{cityId}")
    public ResponseEntity<Weather> read(@PathVariable(name = "cityId") int cityId) {
        final Weather weather = weatherService.read(cityId);

        return weather != null
                ? new ResponseEntity<>(weather, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
