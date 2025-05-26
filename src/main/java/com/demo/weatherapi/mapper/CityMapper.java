package com.demo.weatherapi.mapper;

import com.demo.weatherapi.dto.CityDto;
import com.demo.weatherapi.dto.ForecastDto;
import com.demo.weatherapi.model.City;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CityMapper {

    private final ForecastMapper forecastMapper;

    public CityMapper(ForecastMapper forecastMapper) {
        this.forecastMapper = forecastMapper;
    }

    public CityDto toDto(City city) {
        if (city == null) {
            return null;
        }

        CityDto dto = new CityDto();
        dto.setId(city.getId());
        dto.setCountry(city.getCountry());
        dto.setName(city.getName());

        if (city.getForecasts() != null && !city.getForecasts().isEmpty()) {
            List<ForecastDto> forecastDtos = city.getForecasts().stream()
                    .map(forecast -> {
                        ForecastDto forecastDto = forecastMapper.toDto(forecast);
                        forecastDto.setCityId(city.getId()); // Устанавливаем cityId
                        return forecastDto;
                    })
                    .collect(Collectors.toList());
            dto.setForecasts(forecastDtos);
        }

        return dto;
    }

    public City toEntity(CityDto dto) {
        if (dto == null) {
            return null;
        }

        City city = new City();
        city.setId(dto.getId());
        city.setCountry(dto.getCountry());
        city.setName(dto.getName());

        return city;
    }
}