-- Migration V6: create users table and user_status enum
-- Create table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    person_id UUID REFERENCES persons(id) ON DELETE CASCADE,

    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255),

    external_id       VARCHAR(100),
    external_provider VARCHAR(100),

    nickname VARCHAR(50),
    user_status VARCHAR(50) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT NOT NULL
);

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users(LOWER(username));
