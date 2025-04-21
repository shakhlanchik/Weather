package com.demo.weatherapi.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

@Entity
@Table(name = "weathers")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Weather {
    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "weatherIdSeq", sequenceName = "weather_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "weatherIdSeq")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id")
    @JsonBackReference
    private City city;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "condition")
    private String condition;

    @Column(name = "humidity")
    private Double humidity;

    @Column(name = "wind_speed")
    private Double windSpeed;
}