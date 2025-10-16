-- Migration for creating the phones table
CREATE TYPE phone_category AS ENUM ('HOME', 'WORK', 'PERSONAL', 'OTHER');

CREATE TABLE phones (
    id UUID PRIMARY KEY,
    phone_number VARCHAR(50) NOT NULL,
    category phone_category NOT NULL,
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    person_id UUID NOT NULL REFERENCES persons(id) ON DELETE CASCADE
);