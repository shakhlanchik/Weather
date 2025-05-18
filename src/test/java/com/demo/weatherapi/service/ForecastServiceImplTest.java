package com.demo.weatherapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private static final Integer CITY_ID = 1;
    private static final Integer FORECAST_ID = 1;
    private static final String CITY_NAME = "Moscow";
    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

    @BeforeEach
    void setUp() {
        reset(forecastRepository, cityRepository, forecastMapper, forecastCache);
    }

    private ForecastDto createTestForecastDto(Integer id, LocalDate date,
                                              Double tempMin, Double tempMax) {
        ForecastDto dto = new ForecastDto();
        dto.setId(id);
        dto.setCityId(ForecastServiceImplTest.CITY_ID);
        dto.setDate(date);
        dto.setTemperatureMin(tempMin);
        dto.setTemperatureMax(tempMax);
        return dto;
    }

    private Forecast createTestForecast(Integer id, City city, LocalDate date) {
        Forecast forecast = new Forecast();
        forecast.setId(id);
        forecast.setCity(city);
        forecast.setDate(date);
        return forecast;
    }

    private City createTestCity() {
        City city = new City();
        city.setId(ForecastServiceImplTest.CITY_ID);
        city.setName(ForecastServiceImplTest.CITY_NAME);
        return city;
    }

    @Test
    void readAll_ShouldReturnAllForecasts() {
        City city = createTestCity();
        Forecast forecast1 = createTestForecast(FORECAST_ID, city, TODAY);
        Forecast forecast2 = createTestForecast(FORECAST_ID + 1, city, TOMORROW);
        ForecastDto dto1 = createTestForecastDto(FORECAST_ID, TODAY, -5.0, 5.0);
        ForecastDto dto2 = createTestForecastDto(FORECAST_ID + 1, TOMORROW, -3.0, 4.0);

        when(forecastRepository.findAll()).thenReturn(List.of(forecast1, forecast2));
        when(forecastMapper.toDto(forecast1)).thenReturn(dto1);
        when(forecastMapper.toDto(forecast2)).thenReturn(dto2);

        List<ForecastDto> result = forecastService.readAll();

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(dto1, dto2)));
    }

    @Test
    void readAll_ShouldReturnEmptyListWhenNoForecasts() {
        when(forecastRepository.findAll()).thenReturn(Collections.emptyList());

        List<ForecastDto> result = forecastService.readAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void getForecastsByNameAndDate_ShouldReturnCachedWhenAvailable() {
        ForecastDto cachedDto = createTestForecastDto(FORECAST_ID, TODAY, -5.0, 5.0);
        when(forecastCache.getForecastsByNameAndDate(CITY_NAME, TODAY))
                .thenReturn(List.of(cachedDto));

        List<ForecastDto> result = forecastService.getForecastsByNameAndDate(CITY_NAME, TODAY);

        assertEquals(1, result.size());
        assertEquals(cachedDto, result.get(0));
        verify(forecastRepository, never()).findForecastsByNameAndDate(any(), any());
    }

    @Test
    void getForecastsByNameAndDate_ShouldFetchAndCacheWhenNotCached() {
        City city = createTestCity();
        Forecast forecast = createTestForecast(FORECAST_ID, city, TODAY);
        ForecastDto dto = createTestForecastDto(FORECAST_ID, TODAY, -5.0, 5.0);

        when(forecastCache.getForecastsByNameAndDate(CITY_NAME, TODAY)).thenReturn(null);
        when(forecastRepository.findForecastsByNameAndDate(CITY_NAME, TODAY))
                .thenReturn(List.of(forecast));
        when(forecastMapper.toDto(forecast)).thenReturn(dto);

        List<ForecastDto> result = forecastService.getForecastsByNameAndDate(CITY_NAME, TODAY);

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
        verify(forecastCache).cacheForecastsByNameAndDate(CITY_NAME, TODAY, List.of(dto));
    }

    @Test
    void getForecastsByNameAndDate_ShouldThrowWhenNameOrDateNull() {
        assertThrows(BadRequestException.class,
                () -> forecastService.getForecastsByNameAndDate(null, TODAY));
        assertThrows(BadRequestException.class,
                () -> forecastService.getForecastsByNameAndDate("", TODAY));
        assertThrows(BadRequestException.class,
                () -> forecastService.getForecastsByNameAndDate(CITY_NAME, null));
    }

    @Test
    void getForecastsByCityId_ShouldReturnCachedWhenAvailable() {
        ForecastDto cachedDto = createTestForecastDto(FORECAST_ID, TODAY, -5.0, 5.0);
        when(forecastCache.getForecastsByCityId(CITY_ID)).thenReturn(List.of(cachedDto));

        List<ForecastDto> result = forecastService.getForecastsByCityId(CITY_ID);

        assertEquals(1, result.size());
        assertEquals(cachedDto, result.get(0));
        verify(forecastRepository, never()).findByCityId(any());
    }

    @Test
    void getForecastsByCityId_ShouldFetchAndCacheWhenNotCached() {
        City city = createTestCity();
        Forecast forecast = createTestForecast(FORECAST_ID, city, TODAY);
        ForecastDto dto = createTestForecastDto(FORECAST_ID, TODAY, -5.0, 5.0);

        when(forecastCache.getForecastsByCityId(CITY_ID)).thenReturn(null);
        when(forecastRepository.findByCityId(CITY_ID)).thenReturn(List.of(forecast));
        when(forecastMapper.toDto(forecast)).thenReturn(dto);

        List<ForecastDto> result = forecastService.getForecastsByCityId(CITY_ID);

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
        verify(forecastCache).cacheForecastsByCityId(CITY_ID, List.of(dto));
    }

    @Test
    void getForecastsByCityId_ShouldThrowWhenInvalidCityId() {
        assertThrows(BadRequestException.class,
                () -> forecastService.getForecastsByCityId(null));
        assertThrows(BadRequestException.class,
                () -> forecastService.getForecastsByCityId(0));
        assertThrows(BadRequestException.class,
                () -> forecastService.getForecastsByCityId(-1));
    }

    @Test
    void validateForecastDto_ShouldThrowWhenNullDto() {
        assertThrows(BadRequestException.class,
                () -> forecastService.create(null));
    }

    @Test
    void validateForecastDto_ShouldThrowWhenMandatoryFieldsMissing() {
        ForecastDto dto = new ForecastDto();

        assertThrows(BadRequestException.class,
                () -> forecastService.create(dto));
    }

    @Test
    void createBulk_ShouldThrowWhenEmptyList() {
        assertThrows(BadRequestException.class,
                () -> forecastService.createBulk(null));
        assertThrows(BadRequestException.class,
                () -> forecastService.createBulk(Collections.emptyList()));
    }

    @Test
    void create_ShouldSaveAndCacheForecastWhenValid() {
        ForecastDto inputDto = createTestForecastDto(null, TODAY, -5.0, 5.0);
        City city = createTestCity();
        Forecast entity = createTestForecast(null, city, TODAY);
        Forecast savedEntity = createTestForecast(FORECAST_ID, city, TODAY);
        ForecastDto expectedDto = createTestForecastDto(FORECAST_ID, TODAY, -5.0, 5.0);

        when(cityRepository.findById(CITY_ID)).thenReturn(Optional.of(city));
        when(forecastRepository.existsByCityAndDate(city, TODAY)).thenReturn(false);
        when(forecastMapper.toEntity(inputDto)).thenReturn(entity);
        when(forecastRepository.save(entity)).thenReturn(savedEntity);
        when(forecastMapper.toDto(savedEntity)).thenReturn(expectedDto);

        ForecastDto result = forecastService.create(inputDto);

        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(forecastCache).cacheSingleForecast(expectedDto);
        verify(forecastCache).evictForecastsByCity(CITY_ID);
        verify(forecastCache).evictForecastsByCityAndDate(CITY_ID, TODAY);
    }

    @Test
    void create_ShouldThrowWhenCityNotFound() {
        ForecastDto inputDto = createTestForecastDto(null, TODAY, -5.0, 5.0);
        when(cityRepository.findById(CITY_ID)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> forecastService.create(inputDto));
        verify(forecastRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowWhenDuplicateForecast() {
        ForecastDto inputDto = createTestForecastDto(null, TODAY, -5.0, 5.0);
        City city = createTestCity();

        when(cityRepository.findById(CITY_ID)).thenReturn(Optional.of(city));
        when(forecastRepository.existsByCityAndDate(city, TODAY)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> forecastService.create(inputDto));
        verify(forecastRepository, never()).save(any());
    }

    @Test
    void read_ShouldReturnCachedForecastWhenAvailable() {
        ForecastDto cachedDto = createTestForecastDto(FORECAST_ID, TODAY, -5.0, 5.0);
        when(forecastCache.getForecastById(FORECAST_ID)).thenReturn(cachedDto);

        ForecastDto result = forecastService.read(FORECAST_ID);

        assertEquals(cachedDto, result);
        verify(forecastRepository, never()).findById(any());
    }

    @Test
    void read_ShouldFetchAndCacheWhenNotCached() {
        City city = createTestCity();
        Forecast forecast = createTestForecast(FORECAST_ID, city, TODAY);
        ForecastDto expectedDto = createTestForecastDto(FORECAST_ID, TODAY, -5.0, 5.0);

        when(forecastCache.getForecastById(FORECAST_ID)).thenReturn(null);
        when(forecastRepository.findById(FORECAST_ID)).thenReturn(Optional.of(forecast));
        when(forecastMapper.toDto(forecast)).thenReturn(expectedDto);

        ForecastDto result = forecastService.read(FORECAST_ID);

        assertEquals(expectedDto, result);
        verify(forecastCache).cacheSingleForecast(expectedDto);
    }

    @Test
    void read_ShouldThrowWhenForecastNotFound() {
        when(forecastCache.getForecastById(FORECAST_ID)).thenReturn(null);
        when(forecastRepository.findById(FORECAST_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> forecastService.read(FORECAST_ID));
    }

    @Test
    void update_ShouldUpdateAndCacheWhenValid() {
        ForecastDto inputDto = createTestForecastDto(FORECAST_ID, TOMORROW, -3.0, 4.0);
        City city = createTestCity();
        Forecast existingForecast = createTestForecast(FORECAST_ID, city, TODAY);
        ForecastDto expectedDto = createTestForecastDto(FORECAST_ID, TOMORROW, -3.0, 4.0);

        when(forecastRepository.findById(FORECAST_ID)).thenReturn(Optional.of(existingForecast));
        when(cityRepository.findById(CITY_ID)).thenReturn(Optional.of(city));
        when(forecastRepository.save(existingForecast)).thenReturn(existingForecast);
        when(forecastMapper.toDto(existingForecast)).thenReturn(expectedDto);

        ForecastDto result = forecastService.update(inputDto, FORECAST_ID);

        assertEquals(expectedDto, result);
        verify(forecastCache).cacheSingleForecast(expectedDto);
        verify(forecastCache).evictForecastsByCity(CITY_ID);
        verify(forecastCache).evictForecastsByCityAndDate(CITY_ID, TOMORROW);
    }

    @Test
    void delete_ShouldEvictAllRelatedCacheEntries() {
        City city = createTestCity();
        Forecast forecast = createTestForecast(FORECAST_ID, city, TODAY);

        when(forecastRepository.findById(FORECAST_ID)).thenReturn(Optional.of(forecast));

        forecastService.delete(FORECAST_ID);

        verify(forecastRepository).delete(forecast);
        verify(forecastCache).evictSingleForecast(FORECAST_ID);
        verify(forecastCache).evictForecastsByCity(CITY_ID);
        verify(forecastCache).evictForecastsByCityAndDate(CITY_ID, TODAY);
    }

    @Test
    void validateForecastDto_ShouldThrowWhenInvalidTemperatureRange() {
        ForecastDto dto = createTestForecastDto(null, TODAY, 10.0, 5.0);
        ForecastDto finalDto = dto;
        assertThrows(BadRequestException.class, () -> forecastService.create(finalDto));

        dto = createTestForecastDto(null, TODAY, -101.0, 5.0);
        ForecastDto finalDto1 = dto;
        assertThrows(BadRequestException.class, () -> forecastService.create(finalDto1));

        dto = createTestForecastDto(null, TODAY, -5.0, 101.0);
        ForecastDto finalDto2 = dto;
        assertThrows(BadRequestException.class, () -> forecastService.create(finalDto2));
    }

    @Test
    void createBulk_ShouldProcessAllValidForecasts() {
        ForecastDto dto1 = createTestForecastDto(null, TODAY, -5.0, 5.0);
        ForecastDto dto2 = createTestForecastDto(null, TOMORROW, -3.0, 4.0);
        City city = createTestCity();

        when(cityRepository.findById(CITY_ID)).thenReturn(Optional.of(city));
        when(forecastRepository.existsByCityAndDate(any(), any())).thenReturn(false);
        when(forecastMapper.toEntity(any())).thenReturn(new Forecast());
        when(forecastRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(forecastMapper.toDto(any())).thenAnswer(inv -> {
            Forecast f = inv.getArgument(0);
            return createTestForecastDto(f.getId(), f.getDate(), -5.0, 5.0);
        });

        List<ForecastDto> results = forecastService.createBulk(List.of(dto1, dto2));

        assertEquals(2, results.size());
        verify(forecastRepository, times(2)).save(any());
        verify(forecastCache, times(2)).cacheSingleForecast(any());
    }

    @Test
    void update_ShouldThrowWhenForecastNotFound() {
        ForecastDto inputDto = createTestForecastDto(FORECAST_ID, TODAY, -5.0, 5.0);
        when(forecastRepository.findById(FORECAST_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> forecastService.update(inputDto, FORECAST_ID));
        verify(forecastCache, never()).cacheSingleForecast(any());
    }

    @Test
    void update_ShouldThrowWhenCityNotFound() {
        ForecastDto inputDto = createTestForecastDto(FORECAST_ID, TODAY, -5.0, 5.0);
        Forecast existingForecast = createTestForecast(FORECAST_ID, null, TODAY);

        when(forecastRepository.findById(FORECAST_ID)).thenReturn(Optional.of(existingForecast));
        when(cityRepository.findById(CITY_ID)).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class,
                () -> forecastService.update(inputDto, FORECAST_ID));
        verify(forecastCache, never()).cacheSingleForecast(any());
    }

    @Test
    void delete_ShouldThrowWhenForecastNotFound() {
        when(forecastRepository.findById(FORECAST_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> forecastService.delete(FORECAST_ID));
        verify(forecastCache, never()).evictSingleForecast(any());
        verify(forecastCache, never()).evictForecastsByCity(any());
        verify(forecastCache, never()).evictForecastsByCityAndDate(any(), any());
    }

    @Test
    void validateForecastDto_ShouldThrowWhenDateIsNull() {
        ForecastDto dto = createTestForecastDto(null, null, -5.0, 5.0);
        dto.setDate(null);

        assertThrows(BadRequestException.class, () -> forecastService.create(dto));
    }

    @Test
    void validateForecastDto_ShouldThrowWhenCityIdIsNull() {
        ForecastDto dto = createTestForecastDto(null, TODAY, -5.0, 5.0);
        dto.setCityId(null);

        assertThrows(BadRequestException.class, () -> forecastService.create(dto));
    }
}