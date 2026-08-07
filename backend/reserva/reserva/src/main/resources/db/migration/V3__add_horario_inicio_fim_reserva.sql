ALTER TABLE reserva
    ADD COLUMN horario_inicio VARCHAR(255),
    ADD COLUMN horario_fim VARCHAR(255);

UPDATE reserva
SET horario_inicio = horario
WHERE horario_inicio IS NULL;

ALTER TABLE reserva
DROP COLUMN horario;

CREATE INDEX idx_reserva_ambiente_data_horarios
    ON reserva(ambiente_id, data, horario_inicio, horario_fim);