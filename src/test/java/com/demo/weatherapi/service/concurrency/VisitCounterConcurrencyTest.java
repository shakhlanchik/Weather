package com.demo.weatherapi.service.concurrency;

import com.demo.weatherapi.service.VisitCounterService;
import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisitCounterConcurrencyTest {
    private final VisitCounterService counterService = new VisitCounterService();
    private final String testUrl = "/test-endpoint";

    @Test
    void testConcurrentVisits() throws InterruptedException {
        final int threadCount = 100;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> counterService.incrementVisit(testUrl));
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));

        assertThat(counterService.getVisitCount(testUrl))
                .as("Счетчик должен точно отражать %d запросов", threadCount)
                .isEqualTo(threadCount);
    }
}