INSERT INTO role(name)
VALUES
    ('ROLE_USER'),
    ('ROLE_ADMIN')
;

INSERT INTO users
    (nickname, email, login, password, role_id, first_name, last_name)
VALUES
    ('ghost','ghost@ghost.gst','ghost','',1,'ghost','ghost'),
    ('test','test@test.tst','test','',1,'test','test')
;

