package com.demo.weatherapi.service;

import com.demo.weatherapi.model.City;
import com.demo.weatherapi.repository.CityRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public City create(City city) {
        return cityRepository.save(city);
    }

    public List<City> getAll() {
        return cityRepository.findAll();
    }

    public City getById(Integer id) {
        return cityRepository.findById(id).orElse(null);
    }

    public void delete(Integer id) {
        cityRepository.deleteById(id);
    }
}
