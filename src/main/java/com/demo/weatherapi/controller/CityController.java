package com.demo.weatherapi.controller;

import com.demo.weatherapi.dto.CityDto;
import com.demo.weatherapi.exception.ResourceNotFoundException;
import com.demo.weatherapi.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
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
@Tag(name = "City Controller", description = "Операции с городами")
public class CityController {

    private final CityService cityService;

    public CityController(CityService cityService) {
        this.cityService = cityService;
    }

    @Operation(summary = "Создать город",
        description = "Создает новый город и сохраняет его в базе данных")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Город успешно создан"),
        @ApiResponse(responseCode = "400", description = "Неверный формат запроса")
    })
    @PostMapping
    public ResponseEntity<CityDto> createCity(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Объект города для создания", required = true)
            @RequestBody CityDto cityDto) {
        CityDto createdCity = cityService.create(cityDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCity);
    }

    @Operation(summary = "Получить список всех городов",
            description = "Возвращает список всех городов из базы данных")
    @ApiResponse(responseCode = "200", description = "Список городов успешно получен")
    @GetMapping("/all")
    public ResponseEntity<List<CityDto>> getAll() {
        List<CityDto> cities = cityService.getAll();
        return ResponseEntity.ok(cities);
    }

    @Operation(summary = "Получить город по ID", description = "Возвращает город по указанному ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Город найден"),
        @ApiResponse(responseCode = "404", description = "Город не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CityDto> getCityById(
            @Parameter(description = "ID города", example = "1")
            @PathVariable Integer id) {
        CityDto cityDto = cityService.getCityById(id);
        if (cityDto != null) {
            return ResponseEntity.ok(cityDto);
        }
        throw new ResourceNotFoundException("Город с ID " + id + " не найден");
    }

    @Operation(summary = "Обновить город", description = "Обновляет данные города по указанному ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Город успешно обновлён"),
        @ApiResponse(responseCode = "404", description = "Город не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CityDto> update(
            @Parameter(description = "ID города", example = "1") @PathVariable Integer id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Обновленные данные города", required = true)
            @RequestBody CityDto cityDto) {
        cityDto.setId(id);
        CityDto updatedCity = cityService.update(cityDto);
        if (updatedCity != null) {
            return ResponseEntity.ok(updatedCity);
        }
        throw new ResourceNotFoundException("Город с ID " + id + " не найден");
    }

    @Operation(summary = "Удалить город", description = "Удаляет город по указанному ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Город успешно удалён"),
        @ApiResponse(responseCode = "404", description = "Город не найден")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID города", example = "1") @PathVariable Integer id) {
        if (cityService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        throw new ResourceNotFoundException("Город с ID " + id + " не найден");
    }
}