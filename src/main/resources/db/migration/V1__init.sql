-- Migration V1: Cria tabela users com índice em name
CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT chk_name_min_length CHECK (length(name) >= 5)
);

CREATE INDEX idx_user_name ON users (name);