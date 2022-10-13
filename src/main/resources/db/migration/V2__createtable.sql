CREATE TABLE IF NOT EXISTS Image (
    id SERIAL PRIMARY KEY,
    image VARCHAR(255) UNIQUE NOT NULL,
    imageable_type VARCHAR(255) NOT NULL,
    imageable_id BIGINT NOT NULL
);