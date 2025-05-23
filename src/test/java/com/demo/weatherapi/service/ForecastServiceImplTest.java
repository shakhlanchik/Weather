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

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ForecastServiceImplTest {

    private ForecastRepository forecastRepository;
    private CityRepository cityRepository;
    private ForecastMapper forecastMapper;
    private ForecastCache forecastCache;
    private ForecastServiceImpl forecastService;

    @BeforeEach
    void setUp() {
        forecastRepository = mock(ForecastRepository.class);
        cityRepository = mock(CityRepository.class);
        forecastMapper = mock(ForecastMapper.class);
        forecastCache = mock(ForecastCache.class);
        forecastService = new ForecastServiceImpl(forecastRepository, cityRepository, forecastMapper, forecastCache);
    }

    @Test
    void create_successful() {
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.now(), 10.0, 20.0);
        City city = new City(1, "TestCity");
        Forecast entity = new Forecast();
        Forecast savedEntity = new Forecast();
        ForecastDto savedDto = new ForecastDto(100, 1, dto.getDate(), 10.0, 20.0);

        when(cityRepository.findById(1)).thenReturn(Optional.of(city));
        when(forecastRepository.existsByCityAndDate(city, dto.getDate())).thenReturn(false);
        when(forecastMapper.toEntity(dto)).thenReturn(entity);
        when(forecastRepository.save(entity)).thenReturn(savedEntity);
        when(forecastMapper.toDto(savedEntity)).thenReturn(savedDto);

        ForecastDto result = forecastService.create(dto);

        assertThat(result).isEqualTo(savedDto);
        verify(forecastCache).cacheSingleForecast(savedDto);
        verify(forecastCache).evictForecastsByCity(city.getId());
        verify(forecastCache).evictForecastsByCityAndDate(city.getId(), dto.getDate());
        verify(forecastRepository).save(entity);
    }

    @Test
    void create_throwsIfCityNotFound() {
        ForecastDto dto = new ForecastDto(1, 999, LocalDate.now(), 10.0, 20.0);
        when(cityRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forecastService.create(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Город с ID 999 не найден");
    }

    @Test
    void create_throwsIfForecastExists() {
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.now(), 10.0, 20.0);
        City city = new City(1, "TestCity");

        when(cityRepository.findById(1)).thenReturn(Optional.of(city));
        when(forecastRepository.existsByCityAndDate(city, dto.getDate())).thenReturn(true);

        assertThatThrownBy(() -> forecastService.create(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Прогноз на " + dto.getDate().format(java.time.format.DateTimeFormatter.ISO_DATE) + " для города TestCity уже существует");
    }

    @Test
    void create_throwsIfInvalidTemperature() {
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.now(), 30.0, 20.0);

        assertThatThrownBy(() -> forecastService.create(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Минимальная температура не может быть выше максимальной");
    }

    @Test
    void create_throwsIfTemperatureBelowLimit() {
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.now(), -101.0, 20.0);

        assertThatThrownBy(() -> forecastService.create(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Минимальная температура не может быть ниже -100°C");
    }

    @Test
    void create_throwsIfTemperatureAboveLimit() {
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.now(), 10.0, 101.0);

        assertThatThrownBy(() -> forecastService.create(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Максимальная температура не может быть выше 100°C");
    }

    @Test
    void create_throwsIfDtoIsNull() {
        assertThatThrownBy(() -> forecastService.create(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Некорректные данные прогноза");
    }

    @Test
    void create_throwsIfCityIdIsNull() {
        ForecastDto dto = new ForecastDto(1, null, LocalDate.now(), 10.0, 20.0);

        assertThatThrownBy(() -> forecastService.create(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Некорректные данные прогноза");
    }

    @Test
    void create_throwsIfDateIsNull() {
        ForecastDto dto = new ForecastDto(1, 1, null, 10.0, 20.0);

        assertThatThrownBy(() -> forecastService.create(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Некорректные данные прогноза");
    }

    @Test
    void readAll_successful() {
        Forecast forecast = new Forecast();
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.now(), 10.0, 20.0);
        when(forecastRepository.findAll()).thenReturn(List.of(forecast));
        when(forecastMapper.toDto(forecast)).thenReturn(dto);

        List<ForecastDto> result = forecastService.readAll();

        assertThat(result).hasSize(1).containsExactly(dto);
        verify(forecastRepository).findAll();
        verify(forecastMapper).toDto(forecast);
    }

    @Test
    void read_returnsFromCache() {
        ForecastDto cached = new ForecastDto(1, 1, LocalDate.now(), 10.0, 20.0);
        when(forecastCache.getForecastById(1)).thenReturn(cached);

        ForecastDto result = forecastService.read(1);

        assertThat(result).isEqualTo(cached);
        verifyNoInteractions(forecastRepository);
    }

    @Test
    void read_loadsFromRepoIfNotInCache() {
        Forecast entity = new Forecast();
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.now(), 10.0, 20.0);

        when(forecastCache.getForecastById(1)).thenReturn(null);
        when(forecastRepository.findById(1)).thenReturn(Optional.of(entity));
        when(forecastMapper.toDto(entity)).thenReturn(dto);

        ForecastDto result = forecastService.read(1);

        assertThat(result).isEqualTo(dto);
        verify(forecastCache).cacheSingleForecast(dto);
    }

    @Test
    void read_throwsIfNotFound() {
        when(forecastCache.getForecastById(1)).thenReturn(null);
        when(forecastRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forecastService.read(1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Город с ID 1 не найден");
    }

    @Test
    void update_successful() {
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.now(), 15.0, 25.0);
        City city = new City(1, "TestCity");
        Forecast existing = new Forecast();
        Forecast updatedEntity = new Forecast();
        ForecastDto updatedDto = new ForecastDto(1, 1, dto.getDate(), 15.0, 25.0);

        when(forecastRepository.findById(1)).thenReturn(Optional.of(existing));
        when(cityRepository.findById(1)).thenReturn(Optional.of(city));
        doNothing().when(forecastMapper).updateFromDto(dto, existing);
        when(forecastRepository.save(existing)).thenReturn(updatedEntity);
        when(forecastMapper.toDto(updatedEntity)).thenReturn(updatedDto);

        ForecastDto result = forecastService.update(dto, 1);

        assertThat(result).isEqualTo(updatedDto);
        verify(forecastCache).cacheSingleForecast(updatedDto);
        verify(forecastCache).evictForecastsByCity(city.getId());
        verify(forecastCache).evictForecastsByCityAndDate(city.getId(), dto.getDate());
    }

    @Test
    void update_throwsIfForecastNotFound() {
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.now(), 10.0, 20.0);
        when(forecastRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forecastService.update(dto, 1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Город с ID 1 не найден");
    }

    @Test
    void update_throwsIfCityNotFound() {
        ForecastDto dto = new ForecastDto(1, 999, LocalDate.now(), 10.0, 20.0);
        Forecast existing = new Forecast();
        when(forecastRepository.findById(1)).thenReturn(Optional.of(existing));
        when(cityRepository.findById(999)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forecastService.update(dto, 1))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Город с ID 999 не найден");
    }

    @Test
    void update_throwsIfDtoIsNull() {
        assertThatThrownBy(() -> forecastService.update(null, 1))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Некорректные данные прогноза");
    }

    @Test
    void delete_successful() {
        City city = new City(1, "City");
        Forecast forecast = new Forecast();
        forecast.setId(1);
        forecast.setCity(city);
        forecast.setDate(LocalDate.now());

        when(forecastRepository.findById(1)).thenReturn(Optional.of(forecast));

        forecastService.delete(1);

        verify(forecastRepository).delete(forecast);
        verify(forecastCache).evictSingleForecast(1);
        verify(forecastCache).evictForecastsByCity(city.getId());
        verify(forecastCache).evictForecastsByCityAndDate(city.getId(), forecast.getDate());
    }

    @Test
    void delete_throwsIfNotFound() {
        when(forecastRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> forecastService.delete(1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Город с ID 1 не найден");
    }

    @Test
    void getForecastsByCityId_returnsCached() {
        List<ForecastDto> cached = List.of(new ForecastDto(1, 1, LocalDate.now(), 10.0, 20.0));
        when(forecastCache.getForecastsByCityId(1)).thenReturn(cached);

        List<ForecastDto> result = forecastService.getForecastsByCityId(1);

        assertThat(result).isEqualTo(cached);
        verifyNoInteractions(forecastRepository);
    }

    @Test
    void getForecastsByCityId_loadsFromRepoIfNotCached() {
        Forecast forecast = new Forecast();
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.now(), 10.0, 20.0);
        when(forecastCache.getForecastsByCityId(1)).thenReturn(null);
        when(forecastRepository.findByCityId(1)).thenReturn(List.of(forecast));
        when(forecastMapper.toDto(forecast)).thenReturn(dto);

        List<ForecastDto> result = forecastService.getForecastsByCityId(1);

        assertThat(result).hasSize(1).containsExactly(dto);
        verify(forecastCache).cacheForecastsByCityId(1, result);
    }

    @Test
    void getForecastsByCityId_invalidId() {
        assertThatThrownBy(() -> forecastService.getForecastsByCityId(0))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Некорректный ID города");
    }

    @Test
    void getForecastsByCityId_nullId() {
        assertThatThrownBy(() -> forecastService.getForecastsByCityId(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Некорректный ID города");
    }

    @Test
    void getForecastsByNameAndDate_returnsCached() {
        List<ForecastDto> cached = List.of(new ForecastDto(1, 1, LocalDate.now(), 10.0, 20.0));
        when(forecastCache.getForecastsByNameAndDate("Moscow", LocalDate.now())).thenReturn(cached);

        List<ForecastDto> result = forecastService.getForecastsByNameAndDate("Moscow", LocalDate.now());

        assertThat(result).isEqualTo(cached);
        verifyNoInteractions(forecastRepository);
    }

    @Test
    void getForecastsByNameAndDate_loadsFromRepoIfNotCached() {
        Forecast forecast = new Forecast();
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.now(), 10.0, 20.0);
        when(forecastCache.getForecastsByNameAndDate("Moscow", LocalDate.now())).thenReturn(null);
        when(forecastRepository.findForecastsByNameAndDate("Moscow", LocalDate.now())).thenReturn(List.of(forecast));
        when(forecastMapper.toDto(forecast)).thenReturn(dto);

        List<ForecastDto> result = forecastService.getForecastsByNameAndDate("Moscow", LocalDate.now());

        assertThat(result).hasSize(1).containsExactly(dto);
        verify(forecastCache).cacheForecastsByNameAndDate("Moscow", LocalDate.now(), result);
    }

    @Test
    void getForecastsByNameAndDate_nullArgs() {
        assertThatThrownBy(() -> forecastService.getForecastsByNameAndDate(null, LocalDate.now()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Имя города и дата обязательны");

        assertThatThrownBy(() -> forecastService.getForecastsByNameAndDate("Moscow", null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Имя города и дата обязательны");

        assertThatThrownBy(() -> forecastService.getForecastsByNameAndDate("", LocalDate.now()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Имя города и дата обязательны");
    }

    @Test
    void findByFilters_filtersCorrectly() {
        City city = new City(1, "Moscow");
        Forecast forecast = new Forecast();
        forecast.setCity(city);
        forecast.setDate(LocalDate.of(2023, 1, 1));
        forecast.setTemperatureMin(5.0);
        forecast.setTemperatureMax(10.0);

        when(forecastRepository.findAll()).thenReturn(List.of(forecast));
        when(forecastMapper.toDto(forecast)).thenReturn(new ForecastDto(1, 1, LocalDate.of(2023, 1, 1), 5.0, 10.0));

        List<ForecastDto> result = forecastService.findByFilters("Moscow", LocalDate.of(2023, 1, 1), 4.0, 11.0);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByFilters_emptyResultIfNoMatch() {
        City city = new City(1, "Moscow");
        Forecast forecast = new Forecast();
        forecast.setCity(city);
        forecast.setDate(LocalDate.of(2023, 1, 1));
        forecast.setTemperatureMin(5.0);
        forecast.setTemperatureMax(10.0);

        when(forecastRepository.findAll()).thenReturn(List.of(forecast));

        List<ForecastDto> result = forecastService.findByFilters("London", LocalDate.of(2023, 1, 1), 4.0, 11.0);

        assertThat(result).isEmpty();
    }

    @Test
    void findByFilters_nullParameters() {
        City city = new City(1, "Moscow");
        Forecast forecast = new Forecast();
        forecast.setCity(city);
        forecast.setDate(LocalDate.of(2023, 1, 1));
        forecast.setTemperatureMin(5.0);
        forecast.setTemperatureMax(10.0);
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.of(2023, 1, 1), 5.0, 10.0);

        when(forecastRepository.findAll()).thenReturn(List.of(forecast));
        when(forecastMapper.toDto(forecast)).thenReturn(dto);

        List<ForecastDto> result = forecastService.findByFilters(null, null, null, null);

        assertThat(result).hasSize(1).containsExactly(dto);
    }

    @Test
    void findByFilters_partialParameters() {
        City city = new City(1, "Moscow");
        Forecast forecast = new Forecast();
        forecast.setCity(city);
        forecast.setDate(LocalDate.of(2023, 1, 1));
        forecast.setTemperatureMin(5.0);
        forecast.setTemperatureMax(10.0);
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.of(2023, 1, 1), 5.0, 10.0);

        when(forecastRepository.findAll()).thenReturn(List.of(forecast));
        when(forecastMapper.toDto(forecast)).thenReturn(dto);

        List<ForecastDto> result = forecastService.findByFilters("Moscow", null, 4.0, null);

        assertThat(result).hasSize(1).containsExactly(dto);
    }

    @Test
    void createBulk_successful() {
        ForecastDto dto1 = new ForecastDto(null, 1, LocalDate.now(), 10.0, 20.0);
        ForecastDto dto2 = new ForecastDto(null, 2, LocalDate.now(), 15.0, 25.0);
        City city1 = new City(1, "City1");
        City city2 = new City(2, "City2");
        Forecast entity1 = new Forecast();
        Forecast entity2 = new Forecast();
        Forecast saved1 = new Forecast();
        saved1.setId(1);
        saved1.setCity(city1);
        saved1.setDate(dto1.getDate());
        Forecast saved2 = new Forecast();
        saved2.setId(2);
        saved2.setCity(city2);
        saved2.setDate(dto2.getDate());
        ForecastDto savedDto1 = new ForecastDto(1, 1, LocalDate.now(), 10.0, 20.0);
        ForecastDto savedDto2 = new ForecastDto(2, 2, LocalDate.now(), 15.0, 25.0);

        when(cityRepository.findAllById(Set.of(1, 2))).thenReturn(List.of(city1, city2));
        when(forecastRepository.existsByCityAndDate(any(), any())).thenReturn(false);
        when(forecastMapper.toEntity(dto1)).thenReturn(entity1);
        when(forecastMapper.toEntity(dto2)).thenReturn(entity2);
        when(forecastRepository.saveAll(anyList())).thenReturn(List.of(saved1, saved2));
        when(forecastMapper.toDto(saved1)).thenReturn(savedDto1);
        when(forecastMapper.toDto(saved2)).thenReturn(savedDto2);

        List<ForecastDto> result = forecastService.createBulk(List.of(dto1, dto2));

        assertThat(result).hasSize(2).containsExactly(savedDto1, savedDto2);
        verify(forecastCache).cacheSingleForecast(savedDto1);
        verify(forecastCache).cacheSingleForecast(savedDto2);
        verify(forecastCache).evictForecastsByCity(1);
        verify(forecastCache).evictForecastsByCity(2);
    }

    @Test
    void createBulk_throwsIfNullList() {
        assertThatThrownBy(() -> forecastService.createBulk(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Список прогнозов не может быть null или пустым");
    }

    @Test
    void createBulk_throwsIfEmptyList() {
        assertThatThrownBy(() -> forecastService.createBulk(List.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Список прогнозов не может быть null или пустым");
    }

    @Test
    void createBulk_throwsIfMissingCity() {
        ForecastDto dto = new ForecastDto(null, 10, LocalDate.now(), 10.0, 20.0);
        when(cityRepository.findAllById(Set.of(10))).thenReturn(List.of());

        assertThatThrownBy(() -> forecastService.createBulk(List.of(dto)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Города с ID не найдены");
    }

    @Test
    void createBulk_throwsIfDuplicateExists() {
        ForecastDto dto = new ForecastDto(null, 1, LocalDate.now(), 10.0, 20.0);
        City city = new City(1, "City1");
        when(cityRepository.findAllById(Set.of(1))).thenReturn(List.of(city));
        when(forecastRepository.existsByCityAndDate(city, dto.getDate())).thenReturn(true);

        assertThatThrownBy(() -> forecastService.createBulk(List.of(dto)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Прогноз для города ID 1 на дату " + dto.getDate().format(java.time.format.DateTimeFormatter.ISO_DATE) + " уже существует");
    }

    @Test
    void createBulk_throwsIfInvalidDto() {
        ForecastDto dto = new ForecastDto(null, 1, LocalDate.now(), 30.0, 20.0);
        City city = new City(1, "City1");
        when(cityRepository.findAllById(Set.of(1))).thenReturn(List.of(city));
        when(forecastRepository.existsByCityAndDate(city, dto.getDate())).thenReturn(false);

    }

    @Test
    void updateBulk_successful() {
        ForecastDto dto1 = new ForecastDto(1, 1, LocalDate.now(), 10.0, 20.0);
        ForecastDto dto2 = new ForecastDto(2, 2, LocalDate.now(), 15.0, 25.0);
        City city1 = new City(1, "City1");
        City city2 = new City(2, "City2");
        Forecast existing1 = new Forecast();
        existing1.setId(1);
        Forecast existing2 = new Forecast();
        existing2.setId(2);
        Forecast updated1 = new Forecast();
        Forecast updated2 = new Forecast();
        ForecastDto updatedDto1 = new ForecastDto(1, 1, LocalDate.now(), 10.0, 20.0);
        ForecastDto updatedDto2 = new ForecastDto(2, 2, LocalDate.now(), 15.0, 25.0);

        when(forecastRepository.findAllById(List.of(1, 2))).thenReturn(List.of(existing1, existing2));
        when(cityRepository.findById(1)).thenReturn(Optional.of(city1));
        when(cityRepository.findById(2)).thenReturn(Optional.of(city2));
        doNothing().when(forecastMapper).updateFromDto(dto1, existing1);
        doNothing().when(forecastMapper).updateFromDto(dto2, existing2);
        when(forecastRepository.save(existing1)).thenReturn(updated1);
        when(forecastRepository.save(existing2)).thenReturn(updated2);
        when(forecastMapper.toDto(updated1)).thenReturn(updatedDto1);
        when(forecastMapper.toDto(updated2)).thenReturn(updatedDto2);

        List<ForecastDto> result = forecastService.updateBulk(List.of(dto1, dto2));
    }

    @Test
    void updateBulk_throwsIfNullList() {
        assertThatThrownBy(() -> forecastService.updateBulk(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Список прогнозов не может быть null или пустым");
    }

    @Test
    void updateBulk_throwsIfEmptyList() {
        assertThatThrownBy(() -> forecastService.updateBulk(List.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Список прогнозов не может быть null или пустым");
    }

    @Test
    void updateBulk_throwsIfSomeForecastsNotFound() {
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.now(), 10.0, 20.0);
        when(forecastRepository.findAllById(List.of(1))).thenReturn(List.of());

        assertThatThrownBy(() -> forecastService.updateBulk(List.of(dto)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Некоторые прогнозы не найдены");
    }

    @Test
    void updateBulk_throwsIfInvalidDto() {
        ForecastDto dto = new ForecastDto(1, 1, LocalDate.now(), 30.0, 20.0);
        Forecast existing = new Forecast();
        existing.setId(1);
        when(forecastRepository.findAllById(List.of(1))).thenReturn(List.of(existing));
        when(cityRepository.findById(1)).thenReturn(Optional.of(new City(1, "City1")));
    }

    @Test
    void deleteBulk_successful() {
        City city = new City(1, "City1");
        Forecast forecast1 = new Forecast();
        forecast1.setId(1);
        forecast1.setCity(city);
        forecast1.setDate(LocalDate.now());
        Forecast forecast2 = new Forecast();
        forecast2.setId(2);
        forecast2.setCity(city);
        forecast2.setDate(LocalDate.now());

        when(forecastRepository.findAllById(List.of(1, 2))).thenReturn(List.of(forecast1, forecast2));

        forecastService.deleteBulk(List.of(1, 2));

        verify(forecastRepository).delete(forecast1);
        verify(forecastRepository).delete(forecast2);
        verify(forecastCache).evictSingleForecast(1);
        verify(forecastCache).evictSingleForecast(2);
        verify(forecastCache, times(2)).evictForecastsByCity(1);
        verify(forecastCache, times(2)).evictForecastsByCityAndDate(1, LocalDate.now());
    }

    @Test
    void deleteBulk_throwsIfNullList() {
        assertThatThrownBy(() -> forecastService.deleteBulk(null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Список ID прогнозов не может быть null или пустым");
    }

    @Test
    void deleteBulk_throwsIfEmptyList() {
        assertThatThrownBy(() -> forecastService.deleteBulk(List.of()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Список ID прогнозов не может быть null или пустым");
    }
    @Test
    void deleteBulk_throwsIfSomeForecastsNotFound() {
        when(forecastRepository.findAllById(List.of(1))).thenReturn(List.of());

        assertThatThrownBy(() -> forecastService.deleteBulk(List.of(1)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Некоторые прогнозы не найдены");
    }
}