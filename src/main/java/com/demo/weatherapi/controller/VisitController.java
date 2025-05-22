package com.demo.weatherapi.controller;

import com.demo.weatherapi.service.VisitCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/visits")
@Tag(name = "VisitController", description = "Контроллер для получения статистики посещений")
public class VisitController {

    private final VisitCounterService visitCounterService;

    public VisitController(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @Operation(summary = "Получить статистику посещений",
            description = "Возвращает количество посещений для каждого URL")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Integer>> getVisitCount() {
        return ResponseEntity.ok(visitCounterService.getAllVisits());
    }
}