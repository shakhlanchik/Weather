package com.demo.weatherapi.service;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    private TaskExecutor taskExecutor;

    @InjectMocks
    private LogService logService;

    private final String testDate = "2023-01-01";

    @Test
    void createLogFileAsync_ShouldReturnTaskIdAndStartProcessing() {
        String taskId = logService.createLogFileAsync(testDate);

        assertNotNull(taskId);
        assertEquals(LogService.STATUS_PROCESSING, logService.getTaskInfo(taskId).getStatus());
        verify(taskExecutor).execute(any(Runnable.class));
    }

    @Test
    void getTaskInfo_ShouldReturnNotFoundForInvalidTaskId() {
        LogService.TaskInfo taskInfo = logService.getTaskInfo("invalid-task-id");

        assertEquals(LogService.STATUS_NOT_FOUND, taskInfo.getStatus());
    }

    @Test
    void getTaskStatus_ShouldReturnCorrectStatusMap() {
        String taskId = logService.createLogFileAsync(testDate);

        Map<String, Object> statusMap = logService.getTaskStatus(taskId);

        assertEquals(taskId, statusMap.get("taskId"));
        assertEquals(LogService.STATUS_PROCESSING, statusMap.get("status"));
        assertNotNull(statusMap.get("createdAt"));
        assertNotNull(statusMap.get("lastUpdated"));
        assertEquals("File is being processed", statusMap.get("progress"));
    }

    @Test
    void getLogFileContent_ShouldThrowWhenTaskNotCompleted() {
        String taskId = logService.createLogFileAsync(testDate);

        assertThrows(IOException.class, () -> logService.getLogFileContent(taskId));
    }

    @Test
    void getLogFileStream_ShouldThrowWhenTaskNotCompleted() {
        String taskId = logService.createLogFileAsync(testDate);

        assertThrows(IOException.class, () -> logService.getLogFileStream(taskId));
    }

    @Test
    void updateTaskStatus_ShouldUpdateTaskInfoCorrectly() {
        String taskId = logService.createLogFileAsync(testDate);
        String testMessage = "Test message";

        logService.updateTaskStatus(taskId, LogService.STATUS_FAILED, testMessage);
        LogService.TaskInfo updatedTask = logService.getTaskInfo(taskId);

        assertEquals(LogService.STATUS_FAILED, updatedTask.getStatus());
        assertEquals(testMessage, updatedTask.getMessage());
    }
}