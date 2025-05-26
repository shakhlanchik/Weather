package com.demo.weatherapi.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Getter
@Setter
@Data
@Entity
@Table(name = "cities")
@Schema(description = "Город, к которому относится прогноз погоды")
@EqualsAndHashCode(of = {"id"})
@ToString(exclude = {"forecasts"})
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Идентификатор города", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer id;

    @NotBlank(message = "Название страны не может быть пустым")
    @Size(max = 100, message = "Название страны не должно превышать 100 символов")
    @Column(name = "country", nullable = false)
    @Schema(description = "Название страны", example = "Беларусь", required = true)
    private String country;

    @NotBlank(message = "Название города не может быть пустым")
    @Size(max = 100, message = "Название города не должно превышать 100 символов")
    @Column(name = "name", nullable = false)
    @Schema(description = "Название города", example = "Минск", required = true)
    private String name;

    @OneToMany(mappedBy = "city", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Schema(description = "Список прогнозов погоды, связанных с этим городом")
    private List<Forecast> forecasts = new ArrayList<>();

    @Override
    public String toString() {
        return "City{id=" + id + ", name='" + name + "'}";
    }

    public City() {
        // конструктор по умолчанию (нужен для JPA)
    }

    public City(Integer id, String country, String name) {
        this.id = id;
        this.country = country;
        this.name = name;
    }
}