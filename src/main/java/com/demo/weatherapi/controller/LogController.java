package com.demo.weatherapi.controller;

import com.demo.weatherapi.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "LogController", description = "Контроллер для работы с логами")
public class LogController {
    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @Operation(summary = "Создать лог-файл по дате (асинхронно)")
    @PostMapping("/async")
    public ResponseEntity<Map<String, String>> createLogFileAsync(
            @RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$") String date) {
        String taskId = logService.createLogFileAsync(date);
        return ResponseEntity.accepted().body(Map.of(
                "taskId", taskId,
                "statusUrl", "/api/logs/status/" + taskId,
                "fileUrl", "/api/logs/file/" + taskId
        ));
    }

    @Operation(summary = "Проверить статус задачи")
    @GetMapping("/status/{taskId}")
    public ResponseEntity<LogService.TaskInfo> getTaskStatus(
            @PathVariable String taskId) {
        return ResponseEntity.ok(logService.getTaskInfo(taskId));
    }

    @Operation(summary = "Получить лог-файл по ID задачи")
    @GetMapping("/file/{taskId}")
    public ResponseEntity<Resource> getLogFile(@PathVariable String taskId) {
        try {
            return logService.getLogFileStream(taskId);
        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body(new ByteArrayResource(("Error: " + e.getMessage()).getBytes()));
        }
    }
}