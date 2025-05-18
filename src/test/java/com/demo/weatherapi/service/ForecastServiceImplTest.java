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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForecastServiceImplTest {

    @Mock
    private ForecastRepository forecastRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private ForecastMapper forecastMapper;

    @Mock
    private ForecastCache forecastCache;

    @InjectMocks
    private ForecastServiceImpl forecastService;

    private ForecastDto forecastDto;
    private Forecast forecast;
    private City city;

    @BeforeEach
    void setUp() {
        city = new City();
        city.setId(1);
        city.setName("Moscow");

        forecastDto = new ForecastDto();
        forecastDto.setCityId(1);
        forecastDto.setDate(LocalDate.now());
        forecastDto.setTemperatureMin(-5.0);
        forecastDto.setTemperatureMax(3.0);

        forecast = new Forecast();
        forecast.setId(1);
        forecast.setCity(city);
        forecast.setDate(LocalDate.now());
        forecast.setTemperatureMin(-5.0);
        forecast.setTemperatureMax(3.0);
    }

    @Test
    void create_ValidForecast_ReturnsForecastDto() {
        when(cityRepository.findById(1)).thenReturn(Optional.of(city));
        when(forecastRepository.existsByCityAndDate(city, forecastDto.getDate())).thenReturn(false);
        when(cityRepository.findById(1)).thenReturn(Optional.of(city));
        when(forecastMapper.toEntity(forecastDto)).thenReturn(forecast);
        when(forecastRepository.save(forecast)).thenReturn(forecast);
        when(forecastMapper.toDto(forecast)).thenReturn(forecastDto);

        ForecastDto result = forecastService.create(forecastDto);

        assertNotNull(result);
        assertEquals(forecastDto, result);
        verify(forecastCache).cacheSingleForecast(forecastDto);
    }

    @Test
    void createBulk_ValidList_ReturnsListOfForecastDtos() {
        List<ForecastDto> forecastDtos = List.of(forecastDto, forecastDto);

        when(cityRepository.findById(1)).thenReturn(Optional.of(city));
        when(forecastMapper.toEntity(forecastDto)).thenReturn(forecast);
        when(forecastRepository.save(forecast)).thenReturn(forecast);
        when(forecastMapper.toDto(forecast)).thenReturn(forecastDto);

        List<ForecastDto> results = forecastService.createBulk(forecastDtos);

        assertEquals(2, results.size());
        verify(forecastCache, times(2)).cacheSingleForecast(forecastDto);
    }

    @Test
    void createBulk_EmptyList_ThrowsBadRequestException() {
        assertThrows(BadRequestException.class, () -> forecastService.createBulk(List.of()));
    }

    @Test
    void read_ForecastExists_ReturnsForecastDto() {
        when(forecastCache.getForecastById(1)).thenReturn(null);
        when(forecastRepository.findById(1)).thenReturn(Optional.of(forecast));
        when(forecastMapper.toDto(forecast)).thenReturn(forecastDto);

        ForecastDto result = forecastService.read(1);

        assertEquals(forecastDto, result);
        verify(forecastCache).cacheSingleForecast(forecastDto);
    }

    @Test
    void read_ForecastNotExists_ThrowsResourceNotFoundException() {
        when(forecastCache.getForecastById(1)).thenReturn(null);
        when(forecastRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> forecastService.read(1));
    }

    @Test
    void create_WhenForecastExists_ThrowsBadRequestException() {
        when(cityRepository.findById(1)).thenReturn(Optional.of(city));
        when(forecastRepository.existsByCityAndDate(city, forecastDto.getDate())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> forecastService.create(forecastDto));
    }

    @Test
    void create_WhenInvalidTemperature_ThrowsBadRequestException() {
        forecastDto.setTemperatureMin(1000.0); // Некорректное значение

        assertThrows(BadRequestException.class, () -> forecastService.create(forecastDto));
    }

    @Test
    void update_ValidForecast_ReturnsUpdatedDto() {
        when(forecastRepository.findById(1)).thenReturn(Optional.of(forecast));
        when(cityRepository.findById(1)).thenReturn(Optional.of(city));
        when(forecastRepository.save(forecast)).thenReturn(forecast);
        when(forecastMapper.toDto(forecast)).thenReturn(forecastDto);

        ForecastDto result = forecastService.update(forecastDto, 1);

        assertNotNull(result);
        verify(forecastCache).cacheSingleForecast(forecastDto);
    }

    @Test
    void delete_ExistingForecast_EvictsCache() {
        when(forecastRepository.findById(1)).thenReturn(Optional.of(forecast));

        forecastService.delete(1);

        verify(forecastCache).evictSingleForecast(1);
        verify(forecastCache).evictForecastsByCity(city.getId());
        verify(forecastCache).evictForecastsByCityAndDate(city.getId(), forecast.getDate());
    }

    @Test
    void validateForecastDto_InvalidTemperatures_ThrowsException() {
        forecastDto.setTemperatureMin(100.0);
        forecastDto.setTemperatureMax(-100.0);

        assertThrows(BadRequestException.class,
                () -> forecastService.create(forecastDto));
    }
}