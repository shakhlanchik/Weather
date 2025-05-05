package com.demo.weatherapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CityDto {
    @NotBlank(message = "Имя города не может быть пустым")
    private String name;
}
