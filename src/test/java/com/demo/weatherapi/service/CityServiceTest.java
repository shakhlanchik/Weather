package com.demo.weatherapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.demo.weatherapi.dto.CityDto;
import com.demo.weatherapi.mapper.CityMapper;
import com.demo.weatherapi.model.City;
import com.demo.weatherapi.repository.CityRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @Mock
    private CityMapper cityMapper;

    @InjectMocks
    private CityService cityService;

    @Test
    void create_ShouldSaveAndReturnDto() {
        CityDto inputDto = createTestCityDto(null, "Moscow");
        City entity = createTestCity(null, "Moscow");
        City savedEntity = createTestCity(1, "Moscow");
        CityDto expectedDto = createTestCityDto(1, "Moscow");

        when(cityMapper.toEntity(inputDto)).thenReturn(entity);
        when(cityRepository.save(entity)).thenReturn(savedEntity);
        when(cityMapper.toDto(savedEntity)).thenReturn(expectedDto);

        CityDto result = cityService.create(inputDto);

        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
        assertEquals(expectedDto.getName(), result.getName());
        verify(cityRepository).save(entity);
        verify(cityMapper).toDto(savedEntity);
    }

    @Test
    void getAll_ShouldReturnListOfDtos() {
        City city1 = createTestCity(1, "Moscow");
        City city2 = createTestCity(2, "Berlin");
        CityDto dto1 = createTestCityDto(1, "Moscow");
        CityDto dto2 = createTestCityDto(2, "Berlin");

        when(cityRepository.findAll()).thenReturn(List.of(city1, city2));
        when(cityMapper.toDto(city1)).thenReturn(dto1);
        when(cityMapper.toDto(city2)).thenReturn(dto2);

        List<CityDto> result = cityService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(dto1, result.get(0));
        assertEquals(dto2, result.get(1));
        verify(cityRepository).findAll();
    }

    @Test
    void getCityById_ShouldReturnDtoWhenExists() {
        Integer cityId = 1;
        City city = createTestCity(cityId, "Moscow");
        CityDto expectedDto = createTestCityDto(cityId, "Moscow");

        when(cityRepository.findById(cityId)).thenReturn(Optional.of(city));
        when(cityMapper.toDto(city)).thenReturn(expectedDto);

        CityDto result = cityService.getCityById(cityId);

        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(cityRepository).findById(cityId);
    }

    @Test
    void getCityById_ShouldReturnNullWhenNotExists() {
        Integer cityId = 999;
        when(cityRepository.findById(cityId)).thenReturn(Optional.empty());

        CityDto result = cityService.getCityById(cityId);

        assertNull(result);
        verify(cityRepository).findById(cityId);
        verify(cityMapper, never()).toDto(any());
    }

    @Test
    void delete_ShouldReturnTrueAndDeleteWhenCityExists() {
        Integer cityId = 1;
        when(cityRepository.existsById(cityId)).thenReturn(true);

        boolean result = cityService.delete(cityId);

        assertTrue(result);
        verify(cityRepository).deleteById(cityId);
    }

    @Test
    void delete_ShouldReturnFalseWhenCityNotExists() {
        Integer cityId = 999;
        when(cityRepository.existsById(cityId)).thenReturn(false);

        boolean result = cityService.delete(cityId);

        assertFalse(result);
        verify(cityRepository, never()).deleteById(cityId);
    }

    @Test
    void update_ShouldReturnUpdatedDtoWhenCityExists() {
        Integer cityId = 1;
        CityDto inputDto = createTestCityDto(cityId, "Updated Moscow");
        createTestCity(cityId, "Moscow");
        City updatedCity = createTestCity(cityId, "Updated Moscow");
        CityDto expectedDto = createTestCityDto(cityId, "Updated Moscow");

        when(cityRepository.existsById(cityId)).thenReturn(true);
        when(cityMapper.toEntity(inputDto)).thenReturn(updatedCity);
        when(cityRepository.save(updatedCity)).thenReturn(updatedCity);
        when(cityMapper.toDto(updatedCity)).thenReturn(expectedDto);

        CityDto result = cityService.update(inputDto);

        assertNotNull(result);
        assertEquals(expectedDto, result);
        verify(cityRepository).save(updatedCity);
    }

    @Test
    void update_ShouldReturnNullWhenCityNotExists() {
        Integer cityId = 999;
        CityDto inputDto = createTestCityDto(cityId, "Updated Moscow");
        when(cityRepository.existsById(cityId)).thenReturn(false);

        CityDto result = cityService.update(inputDto);

        assertNull(result);
        verify(cityRepository, never()).save(any());
    }

    private City createTestCity(Integer id, String name) {
        City city = new City();
        city.setId(id);
        city.setName(name);
        return city;
    }

    private CityDto createTestCityDto(Integer id, String name) {
        CityDto dto = new CityDto();
        dto.setId(id);
        dto.setName(name);
        return dto;
    }
}