-- Migration V10: create SYSTEM user and Default App

INSERT INTO persons (id, id_number, name, lastname, is_active, created_at, created_by, version)
VALUES (gen_random_uuid(), 'system', 'System', 'System', true, now(), 'system', 1);

INSERT INTO users (id, person_id, username, password_hash, nickname, user_status, created_at, created_by, version)
VALUES (gen_random_uuid(), (SELECT id FROM persons WHERE id_number = 'system'), 'system', '$2a$10$6NtccM3ugUER/Q2umuTjb.9QST3.g5UpSW6X8ck.U55SXU02MCOSK', 'System', 'RESET', now(), 'system', 1);

INSERT INTO applications (id, name, description, is_active, created_at, created_by, version)
VALUES (gen_random_uuid(), 'UserService', 'User Service Application', true, now(), 'system', 1);

INSERT INTO app_roles (id, application_id, role_name, description, created_at, created_by, version)
VALUES (gen_random_uuid(), (SELECT id FROM applications WHERE name = 'UserService'), 'SYSTEM', 'System', now(), 'system',1);

INSERT INTO app_roles_users (id, user_id, app_role_id, created_at, created_by, version)
VALUES (gen_random_uuid(),
(SELECT id FROM users WHERE username = 'system'),
(SELECT id FROM app_roles WHERE role_name = 'SYSTEM'), now(), 'system', 1);


INSERT INTO default_data (id, name, description, data_category, created_at, created_by, version)
VALUES (gen_random_uuid(), 'ADMIN', 'Administrator role', 'ROLE', now(), 'system', 1);
INSERT INTO default_data (id, name, description, data_category, created_at, created_by, version)
VALUES (gen_random_uuid(), 'USER', 'User role', 'ROLE', now(), 'system', 1);
