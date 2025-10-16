-- Migration to create profiles table
CREATE TABLE profiles (
    id UUID PRIMARY KEY,
    id_person UUID UNIQUE REFERENCES persons(id) ON DELETE CASCADE,

    birthday DATE,
    gender VARCHAR(50),
    nationality VARCHAR(100),
    marital_status VARCHAR(50),
    occupation VARCHAR(100),
    profile_picture_url VARCHAR(255),

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100) NOT NULL,
    updated_at TIMESTAMP,
    updated_by VARCHAR(100),
    version BIGINT NOT NULL
);