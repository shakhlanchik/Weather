package com.demo.weatherapi.service;

import com.demo.weatherapi.cache.ForecastCache;
import com.demo.weatherapi.dto.ForecastDto;
import com.demo.weatherapi.exception.BadRequestException;
import com.demo.weatherapi.exception.ResourceNotFoundException;
import com.demo.weatherapi.mapper.ForecastMapper;
import com.demo.weatherapi.model.City;
import com.demo.weatherapi.model.Forecast;
import com.demo.weatherapi.repository.CityRepository;
import com.demo.weatherapi.repository.ForecastRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ForecastServiceImpl implements ForecastService {

    private final ForecastRepository forecastRepository;
    private final CityRepository cityRepository;
    private final ForecastMapper forecastMapper;
    private final ForecastCache forecastCache;
    String cityWithId = "Город с ID ";
    String notFound = " не найден";

    public ForecastServiceImpl(ForecastRepository forecastRepository,
                               CityRepository cityRepository,
                               ForecastMapper forecastMapper,
                               ForecastCache forecastCache) {
        this.forecastRepository = forecastRepository;
        this.cityRepository = cityRepository;
        this.forecastMapper = forecastMapper;
        this.forecastCache = forecastCache;
    }

    @Override
    @Transactional
    public ForecastDto create(ForecastDto forecastDto) {
        validateForecastDto(forecastDto);

        City city = cityRepository.findById(forecastDto.getCityId()).orElseThrow(() ->
                new BadRequestException(cityWithId + forecastDto.getCityId() + notFound));

        if (forecastRepository.existsByCityAndDate(city, forecastDto.getDate())) {
            throw new BadRequestException(
                    String.format("Прогноз на %s для города %s уже существует",
                            forecastDto.getDate().format(DateTimeFormatter.ISO_DATE),
                            city.getName()));
        }

        Forecast forecast = forecastMapper.toEntity(forecastDto);
        forecast.setCity(city);
        Forecast savedForecast = forecastRepository.save(forecast);
        ForecastDto savedDto = forecastMapper.toDto(savedForecast);

        forecastCache.cacheSingleForecast(savedDto);
        forecastCache.evictForecastsByCity(city.getId());
        forecastCache.evictForecastsByCityAndDate(city.getId(), forecastDto.getDate());

        return savedDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForecastDto> readAll() {
        return forecastRepository.findAll().stream()
                .map(forecastMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ForecastDto read(Integer forecastId) {
        ForecastDto cached = forecastCache.getForecastById(forecastId);
        if (cached != null) {
            return cached;
        }

        Forecast forecast = forecastRepository.findById(forecastId).orElseThrow(() ->
                new ResourceNotFoundException(cityWithId + forecastId + notFound));

        ForecastDto dto = forecastMapper.toDto(forecast);
        forecastCache.cacheSingleForecast(dto);
        return dto;
    }

    @Override
    @Transactional
    public ForecastDto update(ForecastDto forecastDto, Integer forecastId) {
        validateForecastDto(forecastDto);

        Forecast existingForecast = forecastRepository.findById(forecastId).orElseThrow(() ->
                new ResourceNotFoundException(cityWithId + forecastId + notFound));

        City city = cityRepository.findById(forecastDto.getCityId()).orElseThrow(() ->
                new BadRequestException(cityWithId + forecastDto.getCityId() + notFound));

        forecastMapper.updateFromDto(forecastDto, existingForecast);
        existingForecast.setCity(city);

        Forecast updatedForecast = forecastRepository.save(existingForecast);
        ForecastDto updatedDto = forecastMapper.toDto(updatedForecast);

        forecastCache.cacheSingleForecast(updatedDto);
        forecastCache.evictForecastsByCity(city.getId());
        forecastCache.evictForecastsByCityAndDate(city.getId(), forecastDto.getDate());

        return updatedDto;
    }

    @Override
    @Transactional
    public void delete(Integer forecastId) {
        Forecast forecast = forecastRepository.findById(forecastId).orElseThrow(() ->
                new ResourceNotFoundException(cityWithId + forecastId + notFound));

        Integer cityId = forecast.getCity().getId();
        final LocalDate date = forecast.getDate();

        forecastRepository.delete(forecast);

        forecastCache.evictSingleForecast(forecastId);
        forecastCache.evictForecastsByCity(cityId);
        forecastCache.evictForecastsByCityAndDate(cityId, date);

    }

    @Override
    @Transactional(readOnly = true)
    public List<ForecastDto> getForecastsByNameAndDate(String name, LocalDate date) {
        if (name == null || name.isEmpty() || date == null) {
            throw new BadRequestException("Имя города и дата обязательны");
        }

        List<ForecastDto> cached = forecastCache.getForecastsByNameAndDate(name, date);
        if (cached != null) {
            return cached;
        }

        List<ForecastDto> forecasts = forecastRepository.findForecastsByNameAndDate(name, date)
                .stream().map(forecastMapper::toDto).toList();

        forecastCache.cacheForecastsByNameAndDate(name, date, forecasts);
        return forecasts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ForecastDto> getForecastsByCityId(Integer cityId) {
        if (cityId == null || cityId <= 0) {
            throw new BadRequestException("Некорректный ID города");
        }

        List<ForecastDto> cached = forecastCache.getForecastsByCityId(cityId);
        if (cached != null) {
            return cached;
        }

        List<ForecastDto> forecasts = forecastRepository.findByCityId(cityId).stream()
                .map(forecastMapper::toDto).toList();

        forecastCache.cacheForecastsByCityId(cityId, forecasts);
        return forecasts;
    }

    private void validateForecastDto(ForecastDto forecastDto) {
        if (forecastDto == null || forecastDto.getCityId() == null
                || forecastDto.getDate() == null) {
            throw new BadRequestException("Некорректные данные прогноза");
        }

        if (forecastDto.getTemperatureMin() > forecastDto.getTemperatureMax()) {
            throw new BadRequestException(
                "Минимальная температура не может быть выше максимальной");
        }

        if (forecastDto.getTemperatureMin() < -100) {
            throw new BadRequestException("Минимальная температура не может быть ниже -100°C");
        }

        if (forecastDto.getTemperatureMin() > 100) {
            throw new BadRequestException("Минимальная температура не может быть выше 100°C");

        }

        if (forecastDto.getTemperatureMax() > 100) {
            throw new BadRequestException("Максимальная температура не может быть выше 100°C");

        }
    }

    @Override
    @Transactional
    public List<ForecastDto> createBulk(List<ForecastDto> forecastDtos) {
        if (forecastDtos == null || forecastDtos.isEmpty()) {
            throw new BadRequestException("Список прогнозов не может быть пустым");
        }

        return forecastDtos.stream()
                .map(this::create)
                .toList();
    }
}