package com.demo.weatherapi.integration;

import com.demo.weatherapi.service.VisitCounterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class VisitCounterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VisitCounterService visitCounterService;

    @BeforeEach
    public void resetCounters() {
        visitCounterService.resetCounters();
    }

    @Test
    public void testVisitCounting() throws Exception {
        // 1. Проверяем, что эндпоинты доступны
        mockMvc.perform(get("/city/all"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/city/all"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/forecast/all"))
                .andExpect(status().isOk());

        // 2. Проверяем счетчики
        assertThat(visitCounterService.getVisitCount("/city/all")).isEqualTo(2);
        assertThat(visitCounterService.getVisitCount("/forecast/all")).isEqualTo(1);
    }
}