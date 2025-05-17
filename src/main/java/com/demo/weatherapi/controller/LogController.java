package com.demo.weatherapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@Tag(name = "LogController", description = "Контроллер для получения логов приложения")
public class LogController {

    private static final String LOG_PATH = "logs/";

    static {
        DateTimeFormatter.ofPattern("yyyy-MM-dd");
    }

    @Operation(summary = "Получить основные логи по дате", description
            = "Возвращает строки из app.log, начинающиеся с указанной даты (формат yyyy-MM-dd).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Логи успешно получены"),
        @ApiResponse(responseCode = "404", description = "Файл логов не найден"),
        @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/main")
    public ResponseEntity<List<String>> getMainLogsByDate(
            @Parameter(description = "Дата в формате yyyy-MM-dd") @RequestParam String date) {
        try {
            Path logPath = Paths.get(LOG_PATH + "app.log");
            if (!Files.exists(logPath)) {
                return ResponseEntity.notFound().build();
            }

            List<String> filteredLogs = filterLogsByDate(logPath, date);
            return ResponseEntity.ok(filteredLogs);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Получить динамический лог по ID и дате", description
            = "Возвращает строки из файла log-{id}.txt, "
            + "начинающиеся с указанной даты (формат yyyy-MM-dd).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Логи успешно получены"),
        @ApiResponse(responseCode = "404", description = "Файл логов не найден"),
        @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/dynamic/{logId}")
    public ResponseEntity<List<String>> getDynamicLogsByDate(
            @Parameter(description = "Идентификатор лог-файла") @PathVariable String logId,
            @Parameter(description = "Дата в формате yyyy-MM-dd") @RequestParam String date) {
        try {
            Path logPath = Paths.get(LOG_PATH + "log-" + logId + ".txt");
            if (!Files.exists(logPath)) {
                return ResponseEntity.notFound().build();
            }

            List<String> filteredLogs = filterLogsByDate(logPath, date);
            return ResponseEntity.ok(filteredLogs);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Получить список доступных логов", description =
            "Возвращает список всех лог-файлов в формате log-{id}.txt.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список логов успешно получен"),
        @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @GetMapping("/dynamic")
    public ResponseEntity<List<String>> getAvailableDynamicLogs() {
        try {
            List<String> logFiles;
            try (Stream<Path> paths = Files.list(Paths.get(LOG_PATH))) {
                logFiles = paths
                        .filter(this::isValidLogFile)
                        .map(this::extractLogId)
                        .toList();
            }
            return ResponseEntity.ok(logFiles);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private boolean isValidLogFile(Path path) {
        String filename = path.getFileName().toString();
        return filename.startsWith("log-") && filename.endsWith(".txt");
    }

    private String extractLogId(Path path) {
        return path.getFileName().toString()
                .replace("log-", "")
                .replace(".txt", "");
    }

    private List<String> filterLogsByDate(Path logPath, String date) throws IOException {
        try (Stream<String> lines = Files.lines(logPath)) {
            return lines
                    .filter(line -> line.startsWith(date))
                    .toList();
        }
    }

    @GetMapping("/main/download")
    @Operation(summary = "Сформировать лог-файл по дате", description
            = "Формирует отдельный лог-файл за указанную дату и возвращает его для скачивания.")
    public ResponseEntity<Resource> downloadLogByDate(
            @RequestParam @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$") String date) {
        try {
            Path logPath = Paths.get("logs", "app.log").normalize().toAbsolutePath();

            if (!logPath.startsWith(Paths.get("logs").toAbsolutePath())) {
                return ResponseEntity.badRequest().build();
            }

            if (!Files.exists(logPath)) {
                return ResponseEntity.notFound().build();
            }

            List<String> filteredLines = filterLogsByDate(logPath, date);
            if (filteredLines.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            Path tempFile = Files.createTempFile("logs-" + date + "-", ".log");
            Files.write(tempFile, filteredLines);

            Resource resource = new InputStreamResource(Files.newInputStream(tempFile));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=logs-" + date + ".log")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
