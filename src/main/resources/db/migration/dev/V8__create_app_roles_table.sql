-- Migration V8: create app_roles table
-- Adds roles scoped to an application

CREATE TABLE app_roles (
    id UUID PRIMARY KEY,
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    role_name VARCHAR(50) NOT NULL,
    description VARCHAR(100),

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT NOT NULL
);

-- Unique constraint per application
CREATE UNIQUE INDEX IF NOT EXISTS ux_app_roles_app_name ON app_roles(application_id, role_name);

-- Ensure (application_id, id) is unique so it can be referenced by a composite FK
ALTER TABLE app_roles
  ADD CONSTRAINT ux_app_roles_application_id UNIQUE (application_id, id);

CREATE INDEX IF NOT EXISTS idx_app_roles_application_id ON app_roles (application_id);
CREATE INDEX IF NOT EXISTS idx_app_roles_name_lower ON app_roles (lower(role_name));

INSERT INTO app_roles (id, application_id, role_name, description, created_at, created_by, version)
VALUES (
    gen_random_uuid(),
    (SELECT id FROM applications WHERE name = 'UserServiceApp'),
    'USER_SERVICE_ADMIN',
    'Administrator role with full permissions',
    now(),
    'system',
    1);