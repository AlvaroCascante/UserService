-- Migration V9: create user_application_role table
-- Mapping table that assigns a single role to a user for a specific application

CREATE TABLE app_roles_users (
    id UUID PRIMARY KEY,
    user_id UUID UNIQUE REFERENCES users(id),
    app_role_id UUID UNIQUE REFERENCES app_roles(id) ON DELETE CASCADE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT NOT NULL
);

-- Unique constraint: one role per user per application
CREATE UNIQUE INDEX IF NOT EXISTS ux_aru_user_app ON app_roles_users(user_id, app_role_id);

CREATE INDEX IF NOT EXISTS idx_aru_user_id ON app_roles_users(user_id);

