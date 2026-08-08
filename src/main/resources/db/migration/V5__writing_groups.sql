-- Writing groups meet somewhere physical, so each row carries a point on Earth.
--
-- GEOGRAPHY, not GEOMETRY: GEOMETRY does flat-plane math, so distances between
-- lat/lon points come out in *degrees*, which is not a real distance (a degree of
-- longitude is ~69 miles at the equator and ~0 at the poles). GEOGRAPHY does
-- spherical math and answers in metres, correctly, anywhere on the globe.
--
-- SRID 4326 = WGS84, the lat/lon system GPS and mapping APIs speak.
CREATE TABLE writing_groups (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(160) NOT NULL,
    description    TEXT,
    city           VARCHAR(120),
    meeting_format VARCHAR(20) NOT NULL DEFAULT 'in_person',
    location       GEOGRAPHY(POINT, 4326) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT writing_groups_meeting_format_check
        CHECK (meeting_format IN ('in_person', 'online', 'hybrid'))
);

-- Without this index a radius search is a full table scan that computes spherical
-- distance for every row. GiST indexes each point's bounding box, so ST_DWithin can
-- discard almost everything cheaply and run exact math only on the survivors.
CREATE INDEX idx_writing_groups_location ON writing_groups USING GIST (location);

-- A few real groups so the search endpoint returns something on a fresh database.
-- Note the coordinate order: ST_MakePoint takes (longitude, latitude) -- X then Y.
INSERT INTO writing_groups (name, description, city, meeting_format, location) VALUES
    ('Brooklyn Writers Collective', 'Weekly critique group for literary fiction.',
     'Brooklyn, NY', 'in_person',
     ST_SetSRID(ST_MakePoint(-73.9442, 40.6782), 4326)),
    ('Manhattan Novel Lab', 'Long-form novel workshop, meets Tuesdays.',
     'New York, NY', 'in_person',
     ST_SetSRID(ST_MakePoint(-73.9712, 40.7831), 4326)),
    ('Jersey City Drafters', 'Casual write-ins and accountability check-ins.',
     'Jersey City, NJ', 'hybrid',
     ST_SetSRID(ST_MakePoint(-74.0776, 40.7282), 4326)),
    ('Philly Speculative Fiction Guild', 'Sci-fi and fantasy manuscript swaps.',
     'Philadelphia, PA', 'in_person',
     ST_SetSRID(ST_MakePoint(-75.1652, 39.9526), 4326)),
    ('Bay Area Story Structure Club', 'Beat-sheet focused, Save the Cat devotees.',
     'San Francisco, CA', 'in_person',
     ST_SetSRID(ST_MakePoint(-122.4194, 37.7749), 4326));
