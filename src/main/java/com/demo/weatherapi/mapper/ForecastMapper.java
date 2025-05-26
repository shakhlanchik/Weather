package com.demo.weatherapi.mapper;

import com.demo.weatherapi.dto.ForecastDto;
import com.demo.weatherapi.model.Forecast;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class ForecastMapper {

    public ForecastDto toDto(Forecast forecast) {
        if (forecast == null) {
            return null;
        }

        ForecastDto dto = new ForecastDto();
        dto.setId(forecast.getId());
        dto.setDate(forecast.getDate());
        dto.setTemperatureMin(forecast.getTemperatureMin());
        dto.setTemperatureMax(forecast.getTemperatureMax());
        dto.setCondition(forecast.getCondition());
        dto.setHumidity(forecast.getHumidity());
        dto.setWindSpeed(forecast.getWindSpeed());

        if (forecast.getCity() != null) {
            dto.setCityId(forecast.getCity().getId());
        }

        return dto;
    }

    public Forecast toEntity(ForecastDto dto) {
        if (dto == null) {
            return null;
        }

        Forecast forecast = new Forecast();
        updateFromDto(dto, forecast);
        return forecast;
    }

    public void updateFromDto(ForecastDto dto, Forecast entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        if (dto.getDate() != null) {
            entity.setDate(dto.getDate());
        }
        if (dto.getTemperatureMin() != null) {
            entity.setTemperatureMin(dto.getTemperatureMin());
        }
        if (dto.getTemperatureMax() != null) {
            entity.setTemperatureMax(dto.getTemperatureMax());
        }
        if (!ObjectUtils.isEmpty(dto.getCondition())) {
            entity.setCondition(dto.getCondition());
        }
        if (dto.getHumidity() != null) {
            entity.setHumidity(dto.getHumidity());
        }
        if (dto.getWindSpeed() != null) {
            entity.setWindSpeed(dto.getWindSpeed());
        }
    }
}