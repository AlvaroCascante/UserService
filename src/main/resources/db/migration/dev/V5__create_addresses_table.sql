-- V5__create_addresses_table.sql
CREATE TYPE address_category AS ENUM ('HOME', 'WORK', 'OTHER');

CREATE TABLE addresses (
    id UUID PRIMARY KEY,
    address VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    zip_code VARCHAR(20),
    address_type address_category NOT NULL,
    person_id UUID NOT NULL REFERENCES persons(id) ON DELETE CASCADE
);

