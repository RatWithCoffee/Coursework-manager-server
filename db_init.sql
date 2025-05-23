-- Удаление таблиц, если они существуют
DROP TABLE IF EXISTS mark;
DROP TABLE IF EXISTS coursework_record;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS teacher;
DROP TABLE IF EXISTS coursework;
DROP TABLE IF EXISTS "group";

CREATE TYPE user_role AS ENUM ('admin', 'teacher');


CREATE TABLE "user"
(
    id       SERIAL PRIMARY KEY,
    login    VARCHAR(128) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    "role"   user_role    NOT NULL
);
-- Создание таблицы teacher
CREATE TABLE teacher
(
    id INT PRIMARY KEY REFERENCES "user" (id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    job_title VARCHAR(512) NOT NULL
);

-- Создание таблицы coursework
CREATE TABLE coursework
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(512) NOT NULL
);

-- Создание таблицы group (используем кавычки, так как group — зарезервированное слово)
CREATE TABLE "group"
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- Создание таблицы student
CREATE TABLE student
(
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    group_id INT REFERENCES "group" (id) ON DELETE SET NULL
);

-- Создание таблицы coursework_record
CREATE TABLE coursework_record
(
    id         SERIAL PRIMARY KEY,
    cw_id      INT REFERENCES coursework (id) ON DELETE CASCADE,
    teacher_id INT REFERENCES teacher (id) ON DELETE SET NULL,
    group_id   INT REFERENCES "group" (id) ON DELETE CASCADE
);

-- Создание таблицы mark
CREATE TABLE mark
(
    id         SERIAL PRIMARY KEY,
    student_id INT REFERENCES student (id) ON DELETE CASCADE,
    mark       INT CHECK (mark >= 2 AND mark <= 5),
    cw_id      INT REFERENCES coursework(id) ON DELETE CASCADE
);



INSERT INTO "user" (id, login, password, role)
VALUES (1, 'admin', 'admin123', 'admin'),
       (2, 'teacher1', 'teacher123', 'teacher'),
       (3, 'teacher2', 'teacher123', 'teacher'),
       (4, 'teacher3', 'teacher123', 'teacher'),
       (5, 'teacher4', 'teacher123', 'teacher');

ALTER SEQUENCE user_id_seq RESTART WITH 6;

-- Вставка данных в таблицу teacher
INSERT INTO teacher (name, job_title, id)
VALUES ('Иванов Иван Иванович', 'Преподаватель математики', 2),
       ('Петрова Мария Сергеевна', 'Старший преподаватель', 3),
       ('Сидоров Алексей Владимирович', 'Доцент', 4),
       ('Кузнецова Ольга Дмитриевна', 'Профессор', 5);

-- Вставка данных в таблицу coursework
INSERT INTO coursework (name)
VALUES ('Разработка веб-приложения на Java'),
       ('Анализ данных с использованием Python'),
       ('Создание мобильного приложения на Flutter'),
       ('Исследование алгоритмов машинного обучения');

-- Вставка данных в таблицу group
INSERT INTO "group" (name)
VALUES ('Группа 101'),
       ('Группа 102'),
       ('Группа 103'),
       ('Группа 104');

-- Вставка данных в таблицу student
INSERT INTO student (name, group_id)
VALUES
    -- Группа 101
    ('Смирнов Александр', 1),
    ('Ковалева Екатерина', 1),
    ('Иванов Петр', 1),
    ('Сидорова Мария', 1),
    ('Кузнецов Дмитрий', 1),
    ('Петрова Анна', 1),

    -- Группа 102
    ('Морозов Дмитрий', 2),
    ('Новикова Анна', 2),
    ('Лебедев Иван', 2),
    ('Соколова Виктория', 2),
    ('Козлов Алексей', 2),
    ('Павлова Ольга', 2),

    -- Группа 103
    ('Лебедев Павел', 3),
    ('Соколова Виктория', 3),
    ('Иванова Елена', 3),
    ('Петров Сергей', 3),
    ('Сидоров Андрей', 3),
    ('Кузнецова Татьяна', 3),

    -- Группа 104
    ('Козлов Игорь', 4),
    ('Павлова Ольга', 4),
    ('Морозова Екатерина', 4),
    ('Новиков Денис', 4),
    ('Лебедева Марина', 4),
    ('Соколов Артем', 4);

-- Вставка данных в таблицу coursework_record
INSERT INTO coursework_record (cw_id, teacher_id, group_id)
VALUES (1, 2, 1), -- Курсовая 1, Преподаватель 1, Группа 1
       (2, 3, 2), -- Курсовая 2, Преподаватель 2, Группа 2
       (3, 4, 3), -- Курсовая 3, Преподаватель 3, Группа 3
       (4, 5, 4);
-- Курсовая 4, Преподаватель 4, Группа 4

-- Вставка данных в таблицу mark
INSERT INTO mark (student_id, mark, cw_id)
VALUES
    -- Оценки для студентов группы 1 (Курсовая 1)
    (1, 5, 1),  -- Студент 1, Оценка 5, Курсовая 1
    (2, 4, 1),  -- Студент 2, Оценка 4, Курсовая 1
    (3, 5, 1),  -- Студент 3, Оценка 5, Курсовая 1
    (4, 3, 1),  -- Студент 4, Оценка 3, Курсовая 1
    (5, 4, 1),  -- Студент 5, Оценка 4, Курсовая 1
    (6, 5, 1),  -- Студент 6, Оценка 5, Курсовая 1

    -- Оценки для студентов группы 2 (Курсовая 2)
    (7, 5, 2),  -- Студент 7, Оценка 5, Курсовая 2
    (8, 4, 2),  -- Студент 8, Оценка 4, Курсовая 2
    (9, 3, 2),  -- Студент 9, Оценка 3, Курсовая 2
    (10, 5, 2), -- Студент 10, Оценка 5, Курсовая 2
    (11, 4, 2), -- Студент 11, Оценка 4, Курсовая 2
    (12, 5, 2), -- Студент 12, Оценка 5, Курсовая 2

    -- Оценки для студентов группы 3 (Курсовая 3)
    (13, 4, 3), -- Студент 13, Оценка 4, Курсовая 3
    (14, 5, 3), -- Студент 14, Оценка 5, Курсовая 3
    (15, 3, 3), -- Студент 15, Оценка 3, Курсовая 3
    (16, 4, 3), -- Студент 16, Оценка 4, Курсовая 3
    (17, 5, 3), -- Студент 17, Оценка 5, Курсовая 3
    (18, 4, 3), -- Студент 18, Оценка 4, Курсовая 3

    -- Оценки для студентов группы 4 (Курсовая 4)
    (19, 5, 4), -- Студент 19, Оценка 5, Курсовая 4
    (20, 4, 4), -- Студент 20, Оценка 4, Курсовая 4
    (21, 3, 4), -- Студент 21, Оценка 3, Курсовая 4
    (22, 5, 4), -- Студент 22, Оценка 5, Курсовая 4
    (23, 4, 4), -- Студент 23, Оценка 4, Курсовая 4
    (24, 5, 4); -- Студент 24, Оценка 5, Курсовая 4


