-- Migration V10: create default_data table

CREATE TABLE default_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL,
    description VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    data_category VARCHAR(50) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT NOT NULL
);