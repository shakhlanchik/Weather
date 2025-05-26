CREATE TABLE IF NOT EXISTS cities (
    id   SERIAL PRIMARY KEY,
    country VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL
);
CREATE SEQUENCE IF NOT EXISTS cities_id_seq START 1 INCREMENT 1;

CREATE TABLE IF NOT EXISTS forecasts (
    id              SERIAL PRIMARY KEY,
    city_id         INTEGER NOT NULL REFERENCES cities(id),
    date            DATE NOT NULL,
    temperature_min DOUBLE PRECISION NOT NULL,
    temperature_max DOUBLE PRECISION NOT NULL,
    condition       VARCHAR(100) NOT NULL,
    humidity     DOUBLE PRECISION NOT NULL,
    wind_speed   DOUBLE PRECISION NOT NULL
);
CREATE SEQUENCE IF NOT EXISTS forecast_id_seq START 1 INCREMENT 1;

-- DROP TABLE IF EXISTS forecasts;
-- DROP SEQUENCE IF EXISTS forecast_id_seq;
--
-- DROP TABLE IF EXISTS cities;
-- DROP SEQUENCE IF EXISTS cities_id_seq;
