package com.demo.weatherapi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    private static final String LOG_DIR = "logs/";
    private final Map<String, String> taskStatusMap = new ConcurrentHashMap<>();
    private final Map<String, String> taskResultMap = new ConcurrentHashMap<>();

    @Async
    public void createLogFileAsync(String taskId, String date) throws IllegalArgumentException {
        taskStatusMap.put(taskId, "IN_PROGRESS");
        try {
            LocalDate.parse(date);

            Path sourceLogPath = Paths.get(LOG_DIR + "app.log");
            if (!Files.exists(sourceLogPath)) {
                throw new IOException("Исходный лог-файл не найден");
            }

            List<String> filteredLines = Files.lines(sourceLogPath)
                    .filter(line -> line.startsWith(date))
                    .toList();

            if (filteredLines.isEmpty()) {
                taskStatusMap.put(taskId, "FAILED");
                return;
            }

            String fileName = String.format("log-%s-%s.txt", date, taskId);
            Path targetPath = Paths.get(LOG_DIR + fileName);

            Files.write(targetPath, filteredLines, StandardOpenOption.CREATE);
            taskResultMap.put(taskId, fileName);
            taskStatusMap.put(taskId, "COMPLETED");

        } catch (DateTimeParseException e) {
            taskStatusMap.put(taskId, "FAILED");
            logger.error("Неверный формат даты: {}", date, e);
        } catch (Exception e) {
            taskStatusMap.put(taskId, "FAILED");
            logger.error("Ошибка при создании лог-файла", e);
        }
    }

    @Async
    public void createLogFileAsync(String taskId) {
        taskStatusMap.put(taskId, "IN_PROGRESS");
        try {
            Thread.sleep(3000);

            String fileName = "log-" + taskId + ".txt";
            Path filePath = Paths.get(LOG_DIR + fileName);

            String content = "Лог-файл создан. Task ID: " + taskId + "\n";
            Files.writeString(filePath, content, StandardOpenOption.CREATE);

            taskResultMap.put(taskId, fileName);
            taskStatusMap.put(taskId, "COMPLETED");
        } catch (InterruptedException | IOException e) {
            logger.error("Ошибка при создании лог-файла", e);
            taskStatusMap.put(taskId, "FAILED");
        }
    }

    public String getTaskStatus(String taskId) {
        return taskStatusMap.getOrDefault(taskId, "UNKNOWN");
    }

    public String getLogFileContent(String taskId) {
        String fileName = taskResultMap.get(taskId);
        if (fileName == null) {
            return null;
        }

        try {
            Path filePath = Paths.get(LOG_DIR + fileName);
            if (Files.exists(filePath)) {
                return Files.readString(filePath);
            }
        } catch (IOException e) {
            logger.error("Ошибка при чтении лог-файла", e);
        }
        return null;
    }
}