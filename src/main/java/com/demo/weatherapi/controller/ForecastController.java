package com.demo.weatherapi.controller;

import com.demo.weatherapi.dto.ForecastBulkRequest;
import com.demo.weatherapi.dto.ForecastDto;
import com.demo.weatherapi.exception.ResourceNotFoundException;
import com.demo.weatherapi.service.ForecastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forecast")
@Tag(name = "Forecast Controller", description = "Операции с прогнозами погоды")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @Operation(
            summary = "Создать прогноз",
            description = "Создаёт новый прогноз погоды",
            responses = {
                @ApiResponse(responseCode = "201", description = "Прогноз успешно создан",
                        content = @Content(schema = @Schema(implementation = ForecastDto.class))),
                @ApiResponse(responseCode = "400", description = "Ошибка валидации")
            }
    )
    @PostMapping
    public ResponseEntity<ForecastDto> create(@Valid @RequestBody ForecastDto forecastDto) {
        ForecastDto createdForecast = forecastService.create(forecastDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdForecast);
    }

    @Operation(
            summary = "Получить все прогнозы",
            responses = {
                @ApiResponse(responseCode = "200", description = "Список прогнозов",
                        content = @Content(schema = @Schema(implementation = ForecastDto.class))),
                @ApiResponse(responseCode = "204", description = "Прогнозы отсутствуют")
            }
    )
    @GetMapping("/all")
    public ResponseEntity<List<ForecastDto>> readAll() {
        List<ForecastDto> forecasts = forecastService.readAll();
        return forecasts.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(forecasts);
    }

    @Operation(
            summary = "Получить прогноз по ID",
            responses = {
                @ApiResponse(responseCode = "200", description = "Прогноз найден",
                        content = @Content(schema = @Schema(implementation = ForecastDto.class))),
                @ApiResponse(responseCode = "404", description = "Прогноз не найден")
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<ForecastDto> read(
            @Parameter(description = "ID прогноза") @PathVariable Integer id) {
        return ResponseEntity.ok(forecastService.read(id));
    }

    @Operation(
            summary = "Обновить прогноз",
            responses = {
                @ApiResponse(responseCode = "200", description = "Прогноз успешно обновлён",
                        content = @Content(schema = @Schema(implementation = ForecastDto.class))),
                @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
                @ApiResponse(responseCode = "404", description = "Прогноз не найден")
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<ForecastDto> update(
            @Parameter(description = "ID прогноза") @PathVariable Integer id,
            @Valid @RequestBody ForecastDto forecastDto) {
        forecastDto.setId(id);
        return ResponseEntity.ok(forecastService.update(forecastDto, id));
    }

    @Operation(
            summary = "Удалить прогноз по ID",
            responses = {
                @ApiResponse(responseCode = "204", description = "Прогноз успешно удалён"),
                @ApiResponse(responseCode = "404", description = "Прогноз не найден")
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID прогноза") @PathVariable Integer id) {
        forecastService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Найти прогноз по названию города и дате",
            parameters = {
                @Parameter(name = "name", description = "Название города"),
                @Parameter(name = "date", description = "Дата в формате ISO (yyyy-MM-dd)")
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Список прогнозов",
                        content = @Content(schema = @Schema(implementation = ForecastDto.class)))
            }
    )
    @GetMapping("/filter")
    public ResponseEntity<List<ForecastDto>> getForecastsByNameAndDate(
            @RequestParam String name,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(forecastService.getForecastsByNameAndDate(name, date));
    }

    @Operation(
            summary = "Получить все прогнозы по ID города",
            responses = {
                @ApiResponse(responseCode = "200", description = "Список прогнозов",
                        content = @Content(schema = @Schema(implementation = ForecastDto.class))),
                @ApiResponse(responseCode = "404", description = "Прогнозы не найдены")
            }
    )
    @GetMapping("/by-city/{cityId}")
    public ResponseEntity<List<ForecastDto>> getForecastsByCityId(
            @Parameter(description = "ID города") @PathVariable Integer cityId) {
        List<ForecastDto> forecasts = forecastService.getForecastsByCityId(cityId);
        return forecasts.isEmpty()
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(forecasts);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest()
                .body("{\"error\": \"" + ex.getBindingResult().getAllErrors() + "\"}");
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\": \"" + ex.getMessage() + "\"}");
    }

    @Operation(
            summary = "Массовое создание прогнозов",
            description = "Создаёт несколько прогнозов за один запрос",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Прогнозы успешно созданы"),
                    @ApiResponse(responseCode = "400", description = "Ошибка валидации")
            }
    )
    @PostMapping("/bulk")
    public ResponseEntity<List<ForecastDto>> createBulk(
            @Valid @RequestBody ForecastBulkRequest request) {
        List<ForecastDto> createdForecasts = forecastService.createBulk(request.getForecasts());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdForecasts);
    }
}