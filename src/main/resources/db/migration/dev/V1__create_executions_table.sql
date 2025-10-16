CREATE TABLE executions (
    id UUID PRIMARY KEY,
    executed_at TIMESTAMP NOT NULL,
    server_name VARCHAR(255),
    ip_address VARCHAR(255),
    app_version VARCHAR(50),
    environment VARCHAR(50)
);

