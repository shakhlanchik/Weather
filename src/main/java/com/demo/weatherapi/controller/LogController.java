package com.demo.weatherapi.controller;

import com.demo.weatherapi.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "LogController", description = "Контроллер для работы с логами")
@CrossOrigin(origins = "http://localhost:3000")
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
    public ResponseEntity<Map<String, Object>> getTaskStatus(
            @PathVariable String taskId) {

        LogService.TaskInfo task = logService.getTaskInfo(taskId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("taskId", taskId);
        response.put("status", task.getStatus());
        response.put("createdAt", new Date(task.getCreationTime()));

        if (LogService.STATUS_PROCESSING.equals(task.getStatus())) {
            response.put("message", "File is being processed...");
        } else if (task.getMessage() != null) {
            response.put("message", task.getMessage());
        }

        return ResponseEntity.ok(response);
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