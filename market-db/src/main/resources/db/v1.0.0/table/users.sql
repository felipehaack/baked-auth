--liquibase formatted sql

--changeset felipehs:1.0.0
CREATE SEQUENCE users_id_seq START 1000;
CREATE TABLE users
(
    id      INT8          DEFAULT nextval('users_id_seq') NOT NULL,
    name    VARCHAR(255)                                  NOT NULL,
    email   VARCHAR(255)                                  NOT NULL,
    roles   VARCHAR(32)[] DEFAULT '{}'                    NOT NULL,
    status  VARCHAR(32)                                   NOT NULL,
    created TIMESTAMP     DEFAULT now()                   NOT NULL,
    updated TIMESTAMP     DEFAULT now()                   NOT NULL,
    deleted TIMESTAMP,
    UNIQUE (email),
    PRIMARY KEY (id)
);
--rollback DROP TABLE IF EXISTS users CASCADE;
--rollback DROP SEQUENCE users_id_seq;
