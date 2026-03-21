-- Migration for creating the phones table
CREATE TABLE phones (
    id UUID PRIMARY KEY UNIQUE REFERENCES persons(id) ON DELETE CASCADE,
    phone_number VARCHAR(50) NOT NULL,
    category  VARCHAR(50) NOT NULL,
    is_main BOOLEAN NOT NULL DEFAULT FALSE
);