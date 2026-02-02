TRUNCATE TABLE authentication CASCADE;
TRUNCATE TABLE user_gc CASCADE;

insert into authentication
    (id, user_id, password_hash, login_name, last_login_at, failed_attempts, locked, created_at, updated_at, deleted)
values
    ('11111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111112', 'hashPass', 'jonnyjoestar', now(), 0, false, now(), null, false);

insert into user_gc (id, authentication_id, first_name, last_name, email, type, pref_unit, created_at, updated_at, deleted)
values ('11111111-1111-1111-1111-111111111112', '11111111-1111-1111-1111-111111111111', 'Jonny', 'Joestar', 'j.j@wp.pl', 'USER', 'MMOL_PER_L', now(), now(), false );