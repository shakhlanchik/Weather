package com.demo.weatherapi.controller;

import com.demo.weatherapi.service.VisitCounterService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

    @Autowired
    private VisitCounterService counterService;

    @GetMapping("/visit-count")
    public Map<String, Integer> getCounts() {
        return counterService.getAllVisits();
    }
}