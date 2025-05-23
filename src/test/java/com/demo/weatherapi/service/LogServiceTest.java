package com.demo.weatherapi.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

@SpringBootTest
public class LogServiceTest {

    @Autowired
    private LogService logService;

    @Test
    public void testLogFileContent() throws IOException {
        String date = "2023-01-01";
        String taskId = logService.createLogFileAsync(date);

        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> "COMPLETED".equals(logService.getTaskInfo(taskId).getStatus()));

        String logContent = logService.getLogFileContent(taskId);
        assertNotNull(logContent);
        assertTrue(logContent.contains(date));
    }
}