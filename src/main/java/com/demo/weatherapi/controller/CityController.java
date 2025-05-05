package com.demo.weatherapi.controller;

import com.demo.weatherapi.dto.CityDto;
import com.demo.weatherapi.model.City;
import com.demo.weatherapi.repository.CityRepository;
import com.demo.weatherapi.service.CityService;
import jakarta.validation.Valid;
import java.util.List;

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
@RequestMapping("/city")
public class CityController {

    private final CityService cityService;
    private CityRepository cityRepository;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @PostMapping
    public ResponseEntity<?> createCity(@Valid @RequestBody CityDto request) {
        City city = new City();
        city.setName(request.getName());
        cityRepository.save(city);
        return ResponseEntity.ok(city);
    }

    @GetMapping("/all")
    public ResponseEntity<List<City>> getAll() {
        return ResponseEntity.ok(cityService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<City> getById(@PathVariable Integer id) {
        City city = cityService.getById(id);
        return city != null ? ResponseEntity.ok(city) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        cityService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Integer id, @RequestBody City updatedCity) {
        City existingCity = cityService.getById(id);
        if (existingCity == null) {
            return ResponseEntity.notFound().build();
        }

        existingCity.setName(updatedCity.getName());

        cityService.update(existingCity);
        return ResponseEntity.noContent().build();
    }
}