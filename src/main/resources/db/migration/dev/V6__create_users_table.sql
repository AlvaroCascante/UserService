-- Migration V6: create users table and user_status enum
-- This migration creates a `user_status_t` enum and the `users` table.
-- Note: application should store bcrypt (or similar) password hashes in `password_hash`.

CREATE TYPE user_status_t AS ENUM ('ACTIVE', 'INACTIVE', 'BLOCKED', 'RESET');

-- Create table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    person_id UUID REFERENCES persons(id) ON DELETE CASCADE,

    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,

    nickname VARCHAR(50),
    user_status user_status_t NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT NOT NULL
);

-- Indexes
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users(LOWER(username));
