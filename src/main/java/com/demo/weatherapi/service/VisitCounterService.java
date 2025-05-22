package com.demo.weatherapi.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class VisitCounterService {
    private final ConcurrentHashMap<String, AtomicInteger> visitCounters
            = new ConcurrentHashMap<>();

    public void incrementVisit(String url) {
        visitCounters.computeIfAbsent(url, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public int getVisitCount(String url) {
        return visitCounters.getOrDefault(url, new AtomicInteger(0)).get();
    }

    public Map<String, Integer> getAllVisits() {
        return visitCounters.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get()
                ));
    }

    public void resetCounters() {
        visitCounters.clear();
    }
}