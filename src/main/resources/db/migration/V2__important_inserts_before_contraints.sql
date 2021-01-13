INSERT INTO role(name)
VALUES ('ROLE_USER'),
       ('ROLE_ADMIN')
;

INSERT INTO users
(nickname, email, login, password, role_id, first_name, last_name, is_verified)
VALUES ('ghost', 'ghost@ghost.gst', 'ghost', '', 1, 'ghost', 'ghost', true),
       ('qqqqqq', 'qqqqqq@qqqqqq.qqqqqq', 'qqqqqq',
        '$2a$10$t.g9xvzLSWjCkjWSpyeMYu0ELwslWnolZIJUNiGby5NYf4wVRxOEC', 1, 'qqqqqq', 'qqqqqq', true)
;

