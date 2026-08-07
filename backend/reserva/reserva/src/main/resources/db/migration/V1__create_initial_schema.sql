CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE ambiente (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255),
    capacidade INTEGER,
    ficha_tecnica VARCHAR(1000)
);

CREATE TABLE reserva (
    id BIGSERIAL PRIMARY KEY,
    professor VARCHAR(255),
    data VARCHAR(255),
    horario VARCHAR(255),
    status VARCHAR(255),
    ambiente_id BIGINT REFERENCES ambiente(id),
    usuario_id BIGINT REFERENCES usuarios(id)
);

CREATE TABLE notificacao (
    id BIGSERIAL PRIMARY KEY,
    mensagem VARCHAR(255),
    data_hora TIMESTAMP,
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    usuario_id BIGINT REFERENCES usuarios(id)
);

CREATE INDEX idx_reserva_data ON reserva(data);
CREATE INDEX idx_reserva_ambiente_data_horario ON reserva(ambiente_id, data, horario);
CREATE INDEX idx_reserva_usuario ON reserva(usuario_id);
CREATE INDEX idx_notificacao_usuario_data_hora ON notificacao(usuario_id, data_hora DESC);
