-- Create refresh_tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token TEXT NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    client_app VARCHAR(200),
    issued_at TIMESTAMP,
    expires_at TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT false
);

