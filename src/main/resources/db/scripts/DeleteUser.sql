DELETE FROM app_roles_users 
WHERE user_id = '538af60e-f8e7-4344-998b-d861b1cf5166';


DELETE FROM refresh_tokens  
WHERE user_id = '538af60e-f8e7-4344-998b-d861b1cf5166';

DELETE FROM users 
WHERE id = '538af60e-f8e7-4344-998b-d861b1cf5166';