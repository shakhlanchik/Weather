package com.demo.weatherapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
public class LogController {

    private static final String LOG_FOLDER = "logs";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Operation(summary = "Получить лог-файл за дату (формат yyyy-MM-dd)")
    @GetMapping("/{date}")
    public ResponseEntity<FileSystemResource> getLogByDate(@PathVariable String date) {
        LocalDate logDate;
        try {
            logDate = LocalDate.parse(date, FORMATTER);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }

        String filename = String.format("app-%s.log", logDate.format(FORMATTER));
        File file = new File(LOG_FOLDER, filename);

        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(file);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(file.getName()).build());
        headers.setContentType(MediaType.TEXT_PLAIN);

        return ResponseEntity.ok().headers(headers).body(resource);
    }
}
