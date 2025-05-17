package com.demo.weatherapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WeatherApiApplication {
    private static final Logger logger = LoggerFactory.getLogger(WeatherApiApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WeatherApiApplication.class, args);
        logger.info("=== Проверка логов ===");  // Сообщение появится в weatherapi.log
    }
}

//@SpringBootApplication
//public class WeatherApiApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(WeatherApiApplication.class, args);
//    }
//
//}
