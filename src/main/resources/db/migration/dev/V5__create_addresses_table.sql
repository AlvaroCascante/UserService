-- V5__create_addresses_table.sql
CREATE TABLE addresses (
    id UUID PRIMARY KEY UNIQUE REFERENCES persons(id) ON DELETE CASCADE,
    address VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    zip_code VARCHAR(20),
    address_type VARCHAR(20) NOT NULL
);

