package com.demo.weatherapi.model;

import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ForecastTest {

    private final Validator validator;

    public ForecastTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void testForecastEntity() {
        Forecast forecast = new Forecast();
        forecast.setId(42);
        forecast.setDate(LocalDate.of(2025, 5, 11));
        forecast.setTemperatureMin(-5.2);
        forecast.setTemperatureMax(3.8);
        forecast.setCondition("Пасмурно");

        assertEquals(42, forecast.getId());
        assertEquals("Пасмурно", forecast.getCondition());
        assertEquals(3.8, forecast.getTemperatureMax(), 0.001);
    }

    @Test
    void testValidation_MandatoryFields() {
        Forecast forecast = new Forecast();

        var violations = validator.validate(forecast);
        assertEquals(4, violations.size());
    }

    @Test
    void testValidation_ConditionLength() {
        Forecast forecast = new Forecast();
        forecast.setCity(new City());
        forecast.setDate(LocalDate.now());
        forecast.setTemperatureMin(-10.0);
        forecast.setTemperatureMax(10.0);
        forecast.setCondition("A".repeat(256));

        var violations = validator.validate(forecast);
        assertEquals(1, violations.size());
        assertEquals("Описание погодных условий не должно превышать 255 символов",
                violations.iterator().next().getMessage());
    }

    @Test
    void testJsonReferences() {
        City city = new City();
        city.setName("Москва");

        Forecast forecast = new Forecast();
        forecast.setCity(city);
        city.getForecasts().add(forecast);

        assertSame(city, forecast.getCity());
        assertTrue(city.getForecasts().contains(forecast));
    }
}