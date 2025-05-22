package com.demo.weatherapi.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

@SpringBootTest  // Поднимает контекст Spring
public class LogServiceTest {

    @Autowired  // Внедряем сервис
    private LogService logService;

    @Test
    public void testAsyncLogService() {
        // 1. Генерируем уникальный taskId (как в вашем контроллере)
        String taskId = UUID.randomUUID().toString();

        // 2. Запускаем задачу с этим taskId
        logService.createLogFileAsync(taskId);  // Теперь передаем taskId

        // 3. Проверяем статус
        await().atMost(5, SECONDS).until(() ->
                "COMPLETED".equals(logService.getTaskStatus(taskId))
        );

        // 4. Проверяем содержимое файла
        String logContent = logService.getLogFileContent(taskId);
        assertTrue(logContent.contains(taskId));
    }
}