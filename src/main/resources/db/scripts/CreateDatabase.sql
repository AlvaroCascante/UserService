CREATE DATABASE user_service_dev_db;
-- Swith to the database

CREATE SCHEMA IF NOT EXISTS user_service_schema_dev;

CREATE USER user_service_user_dev WITH PASSWORD '$2a$10$EsaWqAIVcnkKsrxuAsZPcuhJrxgauRijndtDJhRDUM5gduSxwb/MK';

-- Allow connection
GRANT CONNECT ON DATABASE user_service_dev_db TO user_service_user_dev;
GRANT USAGE ON SCHEMA user_service_schema_dev TO user_service_user_dev;

-- Allow table access
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA user_service_schema_dev TO user_service_user_dev;

-- Allow sequences (for IDs)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA user_service_schema_dev TO user_service_user_dev;

GRANT ALL ON SCHEMA user_service_schema_dev TO user_service_user_dev;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA user_service_schema_dev TO user_service_user_dev;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA user_service_schema_dev TO user_service_user_dev;