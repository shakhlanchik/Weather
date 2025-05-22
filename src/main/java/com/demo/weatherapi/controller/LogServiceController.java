package com.demo.weatherapi.controller;

import com.demo.weatherapi.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs/v2")
@Tag(name = "LogServiceController", description = "Асинхронный сервис для работы с лог-файлами")
public class LogServiceController {

    private final LogService logService;

    public LogServiceController(LogService logService) {
        this.logService = logService;
    }

    @Operation(summary = "Создать лог-файл", description
            = "Асинхронно создает новый лог-файл и возвращает ID задачи")
    @ApiResponse(responseCode = "202", description = "Задача на создание лог-файла принята")
    @PostMapping("/create")
    public ResponseEntity<String> createLogFile(
            @RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$") String date) {

        String taskId = UUID.randomUUID().toString();
        logService.createLogFileAsync(taskId, date);
        return ResponseEntity.accepted().body(taskId);
    }

    @Operation(summary = "Получить статус задачи", description
            = "Проверяет статус выполнения задачи по ID")
    @ApiResponse(responseCode = "200", description = "Статус задачи получен")
    @GetMapping("/status/{taskId}")
    public ResponseEntity<String> getStatus(
            @Parameter(description = "ID задачи") @PathVariable String taskId) {
        String status = logService.getTaskStatus(taskId);
        return ResponseEntity.ok(status);
    }

    @Operation(summary = "Получить лог-файл", description
            = "Возвращает содержимое лог-файла по ID задачи")
    @ApiResponse(responseCode = "200", description = "Лог-файл получен")
    @ApiResponse(responseCode = "404", description = "Файл не найден")
    @GetMapping("/file/{taskId}")
    public ResponseEntity<String> getLogFile(
            @Parameter(description = "ID задачи") @PathVariable String taskId) {
        String logContent = logService.getLogFileContent(taskId);
        if (logContent == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(logContent);
    }
}