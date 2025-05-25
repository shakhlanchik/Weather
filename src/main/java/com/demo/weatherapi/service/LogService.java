package com.demo.weatherapi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class LogService {
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    private static final String LOG_DIR = "logs/";

    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_NOT_FOUND = "NOT_FOUND";

    private final Map<String, TaskInfo> tasks = new ConcurrentHashMap<>();
    private final TaskExecutor taskExecutor;

    public LogService(@Qualifier("taskExecutor") TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
        createLogsDirectory();
    }

    private void createLogsDirectory() {
        try {
            Path path = Paths.get(LOG_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Created logs directory: {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Failed to create logs directory", e);
        }
    }

    public String createLogFileAsync(String date) {
        String taskId = UUID.randomUUID().toString();
        TaskInfo task = new TaskInfo(STATUS_PROCESSING, System.currentTimeMillis());
        tasks.put(taskId, task);

        taskExecutor.execute(() -> {
            try {
                Thread.sleep(10000);

                Path logDir = Paths.get(LOG_DIR);
                if (!Files.exists(logDir)) {
                    Files.createDirectories(logDir);
                }

                Path sourceLogPath = Paths.get(LOG_DIR, "app.log");
                if (!Files.exists(sourceLogPath)) {
                    throw new IOException("Source log file not found");
                }

                List<String> filteredLines = Files.lines(sourceLogPath)
                        .filter(line -> line.startsWith(date))
                        .toList();

                if (filteredLines.isEmpty()) {
                    updateTaskStatus(taskId, STATUS_FAILED, "No logs for date");
                    return;
                }

                String fileName = "log-" + date + "-" + taskId + ".log";
                Path targetPath = Paths.get(LOG_DIR, fileName);

                synchronized (this) {
                    Files.write(targetPath, filteredLines,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE);
                }

                updateTaskStatus(taskId, STATUS_COMPLETED, fileName);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                updateTaskStatus(taskId, STATUS_FAILED, e.getMessage());
                logger.error(e.getMessage());
            } catch (Exception e) {
                updateTaskStatus(taskId, STATUS_FAILED, e.getMessage());
                logger.error(e.getMessage());
            }
        });

        return taskId;
    }

    void updateTaskStatus(String taskId, String status, String messageOrFile) {
        TaskInfo task = tasks.get(taskId);
        if (task != null) {
            task.setStatus(status);
            if (status.equals(STATUS_COMPLETED)) {
                task.setResultFile(messageOrFile);
            } else {
                task.setMessage(messageOrFile);
            }
            task.setLastUpdated(System.currentTimeMillis());
            logger.info("Task {} status updated to {}", taskId, status);
        }
    }

    public TaskInfo getTaskInfo(String taskId) {
        return tasks.getOrDefault(taskId,
                new TaskInfo(STATUS_NOT_FOUND, System.currentTimeMillis()));
    }

    public ResponseEntity<Resource> getLogFileStream(String taskId) throws IOException {
        TaskInfo task = tasks.get(taskId);
        if (task == null || !STATUS_COMPLETED.equals(task.getStatus())) {
            throw new IOException("Task not ready. Current status: " +
                    (task != null ? task.getStatus() : "null"));
        }

        Path filePath = Paths.get(LOG_DIR, task.getResultFile());
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath.getFileName());
        }

        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filePath.getFileName() + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(Files.size(filePath))
                .body(resource);
    }

    public void getLogFileContent(String taskId) throws IOException {
        TaskInfo task = tasks.get(taskId);
        if (task == null || !STATUS_COMPLETED.equals(task.getStatus())) {
            throw new IOException("Task not ready. Current status: " +
                    (task != null ? task.getStatus() : "null"));
        }

        Path filePath = Paths.get(LOG_DIR, task.getResultFile());
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + filePath.getFileName());
        }

        Files.readAllBytes(filePath);
    }

    public Map<String, Object> getTaskStatus(String taskId) {
        TaskInfo task = getTaskInfo(taskId);
        Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("taskId", taskId);
        response.put("status", task.getStatus());
        response.put("createdAt", new Date(task.getCreationTime()));
        response.put("lastUpdated", new Date(task.getLastUpdated()));

        if (STATUS_PROCESSING.equals(task.getStatus())) {
            response.put("progress", "File is being processed");
        } else if (task.getMessage() != null) {
            response.put("message", task.getMessage());
        } else if (task.getResultFile() != null) {
            response.put("file", task.getResultFile());
        }

        return response;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class TaskInfo {
        private String status;
        private long creationTime;
        private long lastUpdated;
        private String message;
        private String resultFile;

        public TaskInfo(String status, long creationTime) {
            this(status, creationTime, creationTime, null, null);
        }

        public void setMessage(String message) {
            this.message = message;
            this.lastUpdated = System.currentTimeMillis();
        }

        public void setResultFile(String resultFile) {
            this.resultFile = resultFile;
            this.lastUpdated = System.currentTimeMillis();
        }
    }
}