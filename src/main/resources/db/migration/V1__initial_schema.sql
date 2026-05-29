CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100) NOT NULL,
    timezone      VARCHAR(64)  NOT NULL DEFAULT 'UTC',
    location      GEOGRAPHY(POINT, 4326),
    city          VARCHAR(120),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE story_structures (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    slug        VARCHAR(120) UNIQUE NOT NULL,
    description TEXT,
    is_system   BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE structure_beats (
    id           BIGSERIAL PRIMARY KEY,
    structure_id BIGINT NOT NULL REFERENCES story_structures(id) ON DELETE CASCADE,
    name         VARCHAR(160) NOT NULL,
    description  TEXT,
    position     INT NOT NULL,
    UNIQUE (structure_id, position)
);