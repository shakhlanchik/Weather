CREATE TABLE IF NOT EXISTS cities (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);
CREATE SEQUENCE IF NOT EXISTS cities_id_seq START 1 INCREMENT 1;

-- CREATE TABLE IF NOT EXISTS weathers (
--     id           SERIAL PRIMARY KEY,
--     city_id      INTEGER NOT NULL REFERENCES cities(id),
--     temperature  DOUBLE PRECISION NOT NULL,
--     condition    VARCHAR(100) NOT NULL,
--     humidity     DOUBLE PRECISION NOT NULL,
--     wind_speed   DOUBLE PRECISION NOT NULL
-- );
-- CREATE SEQUENCE IF NOT EXISTS weather_id_seq START 1 INCREMENT 1;

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

-- DROP TABLE IF EXISTS weathers;
-- DROP SEQUENCE IF EXISTS weather_id_seq;
--
-- DROP TABLE IF EXISTS forecasts;
-- DROP SEQUENCE IF EXISTS forecast_id_seq;
--
-- DROP TABLE IF EXISTS cities;
-- DROP SEQUENCE IF EXISTS cities_id_seq;
