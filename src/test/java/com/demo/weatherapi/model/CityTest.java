package com.demo.weatherapi.model;

import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import static org.junit.jupiter.api.Assertions.*;

class CityTest {

    private final Validator validator;

    public CityTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    void testCityEntity() {
        City city = new City();
        city.setId(1);
        city.setName("Москва");

        assertEquals(1, city.getId());
        assertEquals("Москва", city.getName());
        assertNotNull(city.getForecasts());
    }

    @Test
    void testValidation_NameBlank() {
        City city = new City();
        city.setName("");

        var violations = validator.validate(city);
        assertEquals(1, violations.size());
        assertEquals("Название города не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_NameTooLong() {
        City city = new City();
        city.setName("A".repeat(101));

        var violations = validator.validate(city);
        assertEquals(1, violations.size());
        assertEquals("Название города не должно превышать 100 символов", violations.iterator().next().getMessage());
    }

    @Test
    void testBidirectionalRelationship() {
        City city = new City();
        city.setName("Москва");

        Forecast forecast = new Forecast();
        forecast.setCity(city);
        city.getForecasts().add(forecast);

        assertEquals(1, city.getForecasts().size());
        assertSame(city, forecast.getCity());
    }

    @Test
    void testToString() {
        City city = new City();
        city.setId(1);
        city.setName("Москва");

        assertEquals("City{id=1, name='Москва'}", city.toString());
    }
}