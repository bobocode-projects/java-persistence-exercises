CREATE TABLE persons
(
    id         INT PRIMARY KEY,
    first_name VARCHAR(32),
    last_name  VARCHAR(32)
);

CREATE TABLE notes
(
    id        INT PRIMARY KEY,
    body      VARCHAR(128),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    person_id INT REFERENCES persons
);

INSERT INTO persons(id, first_name, last_name)
VALUES (1, 'Martin', 'Fowler'),
       (2, 'Joshua', 'Bloch'),
       (3, 'Herbert', 'Schildt'),
       (4, 'Vlad', 'Mihalcea'),
       (5, 'Robert', 'Martin');

INSERT INTO notes(id, body, person_id)
VALUES (1, 'I should start focusing more on NoSQL probably', 1),
       (2, 'It seems that persistence is pretty hard topic', 1),
       (3, 'Ah, nobody cares about the UI of my website', 1),
       (4, 'People don`t realize how valuable is my book Effective Java', 2),
       (5, 'I`m too old for this sh*t', 3),
       (6, 'Don`t let people to start writing a custom ORM', 4),
       (7, 'I am the only person who truly understand how the Hibernate works', 4),
       (8, 'It doesn`t matter if it`s Hibernate or Bibernate. Anyway it should be CLEAN!!!', 4);