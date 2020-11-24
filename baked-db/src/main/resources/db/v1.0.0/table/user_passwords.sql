--liquibase formatted sql

--changeset felipehs:1.0.0
CREATE TABLE user_passwords
(
    user_id            INT8                    NOT NULL,
    encrypted_password VARCHAR(255)            NOT NULL,
    reset_token        VARCHAR(255),
    reset_sent         TIMESTAMP,
    updated            TIMESTAMP DEFAULT now() NOT NULL
);
--rollback DROP TABLE IF EXISTS user_passwords CASCADE;
