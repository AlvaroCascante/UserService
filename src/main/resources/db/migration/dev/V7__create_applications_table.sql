-- Migration for creating the applications table
CREATE TABLE applications (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT NOT NULL
);

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_applications_name ON applications(name);

INSERT INTO applications (id, name, description, is_active, created_at, created_by, version)
VALUES (
    gen_random_uuid(),
    'UserServiceApp',
    'User Service Application',
    TRUE,
    now(),
    'system',
    1);

