CREATE TABLE novels (
    id BIGSERIAL PRIMARY KEY,
    structure_id BIGINT REFERENCES story_structures(id),
    title VARCHAR(255) NOT NULL,
    premise TEXT, 
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE novel_beats (
    id BIGSERIAL PRIMARY KEY,
    novel_id BIGINT NOT NULL REFERENCES novels(id) ON DELETE CASCADE,
    structure_beat_id BIGINT NOT NULL REFERENCES structure_beats(id),
    notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'empty',
    UNIQUE (novel_id, structure_beat_id)
);