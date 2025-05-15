package com.demo.weatherapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Log Controller",
        description = "Эндпоинты для получения логов приложения по дате"
)
@RestController
@RequestMapping("/logs")
public class LogController {

    private static final String LOGS_DIR = "logs";

    @Operation(
            summary = "Получить логи по дате", description =
            "Возвращает содержимое лог-файла за указанную дату. Формат даты: yyyy-MM-dd. "
                    + "Ищет файл вида app-yyyy-MM-dd.log в папке logs."
    )
    @GetMapping("/{date}")
    public ResponseEntity<Object> getLogsByDate(
            @Parameter(description = "Дата в формате yyyy-MM-dd", example = "2024-05-01")
            @PathVariable String date
    ) {
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return ResponseEntity.badRequest().body("Invalid date format. Use yyyy-MM-dd.");
        }

        Path logPath = Paths.get(LOGS_DIR, "app-" + date + ".log");

        if (!Files.exists(logPath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Log file not found.");
        }

        try (BufferedReader reader = Files.newBufferedReader(logPath)) {
            List<String> lines = reader.lines().toList();

            if (lines.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Log file is empty for date: " + date);
            }

            return ResponseEntity.ok(lines);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error reading log file.");
        }
    }
}