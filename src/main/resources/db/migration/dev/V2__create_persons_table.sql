-- Migration for creating the persons table
CREATE TABLE persons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_number VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    lastname VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT NOT NULL
);

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_persons_id_number ON persons(id_number);

