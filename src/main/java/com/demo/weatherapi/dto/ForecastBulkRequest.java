package com.demo.weatherapi.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForecastBulkRequest {
    private List<ForecastDto> forecasts;
}