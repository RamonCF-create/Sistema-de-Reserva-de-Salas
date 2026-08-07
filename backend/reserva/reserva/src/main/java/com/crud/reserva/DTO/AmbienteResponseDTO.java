package com.crud.reserva.DTO;

import com.crud.reserva.model.Ambiente;

public record AmbienteResponseDTO(
        Long id,
        String nome,
        Integer capacidade,
        String fichaTecnica
) {
    public static AmbienteResponseDTO from(Ambiente ambiente) {
        if (ambiente == null) {
            return null;
        }

        return new AmbienteResponseDTO(
                ambiente.getId(),
                ambiente.getNome(),
                ambiente.getCapacidade(),
                ambiente.getFichaTecnica()
        );
    }
}
