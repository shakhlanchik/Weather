package com.demo.weatherapi.service;

import com.demo.weatherapi.dto.CityDto;
import com.demo.weatherapi.mapper.CityMapper;
import com.demo.weatherapi.model.City;
import com.demo.weatherapi.repository.CityRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    public CityService(CityRepository cityRepository, CityMapper cityMapper) {
        this.cityRepository = cityRepository;
        this.cityMapper = cityMapper;
    }

    @Transactional
    public CityDto create(CityDto cityDto) {
        City city = cityMapper.toEntity(cityDto);
        City savedCity = cityRepository.save(city);
        return cityMapper.toDto(savedCity);
    }

    @Transactional(readOnly = true)
    public List<CityDto> getAll() {
        return cityRepository.findAll().stream()
                .map(cityMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CityDto getCityById(Integer id) {
        return cityRepository.findById(id)
                .map(cityMapper::toDto)
                .orElse(null);
    }

    @Transactional
    public boolean delete(Integer id) {
        if (cityRepository.existsById(id)) {
            cityRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public CityDto update(CityDto cityDto) {
        if (cityDto.getId() == null || !cityRepository.existsById(cityDto.getId())) {
            return null;
        }
        City city = cityMapper.toEntity(cityDto);
        City updatedCity = cityRepository.save(city);
        return cityMapper.toDto(updatedCity);
    }
}