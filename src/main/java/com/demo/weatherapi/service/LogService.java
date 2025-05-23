package com.demo.weatherapi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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
    private static final long TASK_TTL_HOURS = 24;

    private final Map<String, TaskInfo> tasks = new ConcurrentHashMap<>();
    private final TaskExecutor taskExecutor;

    public LogService(@Qualifier("taskExecutor") TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public String createLogFileAsync(String date) {
        String taskId = UUID.randomUUID().toString();
        TaskInfo task = new TaskInfo("PENDING", System.currentTimeMillis());
        tasks.put(taskId, task);

        taskExecutor.execute(() -> {
            try {
                Path logDir = Paths.get(LOG_DIR);
                if (!Files.exists(logDir)) {
                    Files.createDirectories(logDir);
                }

                tasks.put(taskId, task.updateStatus("PROCESSING"));

                Path sourceLogPath = Paths.get(LOG_DIR + "app.log");
                if (!Files.exists(sourceLogPath)) {
                    throw new IOException("Source log file not found");
                }

                List<String> filteredLines = Files.lines(sourceLogPath)
                        .filter(line -> line.startsWith(date))
                        .toList();

                if (filteredLines.isEmpty()) {
                    tasks.put(taskId, task.updateStatus("FAILED").setMessage("No logs for date"));
                    return;
                }

                String fileName = "log-" + date + "-" + taskId + ".txt";
                Path targetPath = Paths.get(LOG_DIR + fileName);

                synchronized (this) {
                    Files.write(targetPath, filteredLines,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.WRITE);
                }

                tasks.put(taskId, task.updateStatus("COMPLETED").setResultFile(fileName));

            } catch (Exception e) {
                tasks.put(taskId, task.updateStatus("FAILED").setMessage(e.getMessage()));
                logger.error("Error creating log file: {}", e.getMessage());
            }
        });

        cleanupOldTasks();
        return taskId;
    }

    private void cleanupOldTasks() {
        long threshold = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(TASK_TTL_HOURS);
        tasks.entrySet().removeIf(entry ->
                entry.getValue().getCreationTime() < threshold
        );
    }

    public TaskInfo getTaskInfo(String taskId) {
        return tasks.getOrDefault(taskId, new TaskInfo("NOT_FOUND", 0));
    }

    public ResponseEntity<Resource> getLogFileStream(String taskId) throws IOException {
        TaskInfo task = tasks.get(taskId);
        if (task == null || !"COMPLETED".equals(task.getStatus())) {
            throw new IOException("Task not ready");
        }

        Path filePath = Paths.get(LOG_DIR, task.getResultFile());
        if (!Files.exists(filePath)) {
            throw new IOException("File not found");
        }

        Resource resource = new FileSystemResource(filePath);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filePath.getFileName() + "\"")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(Files.size(filePath))
                .body(resource);
    }

    public String getLogFileContent(String taskId) throws IOException {
        TaskInfo task = tasks.get(taskId);
        if (task == null || !"COMPLETED".equals(task.getStatus())) {
            throw new IOException("Task not ready");
        }

        Path filePath = Paths.get(LOG_DIR, task.getResultFile());
        if (!Files.exists(filePath)) {
            throw new IOException("File not found");
        }

        return new String(Files.readAllBytes(filePath));
    }

    public String getTaskStatus(String taskId) {
        return null;
    }

    @Getter
    @Setter

    @AllArgsConstructor
    public static class TaskInfo {
        private String status;
        private long creationTime;
        private String message;
        private String resultFile;

        public TaskInfo(String status, long creationTime) {
            this(status, creationTime, null, null);
        }

        public TaskInfo updateStatus(String status) {
            this.status = status;
            return this;
        }

        public TaskInfo setMessage(String message) {
            this.message = message;
            return this;
        }

        public TaskInfo setResultFile(String resultFile) {
            this.resultFile = resultFile;
            return this;
        }
    }
}