package com.crud.reserva.DTO;

import com.crud.reserva.model.Reserva;

public record ReservaResponseDTO(
        Long id,
        String professor,
        String data,
        String horario,
        String status,
        AmbienteResponseDTO ambiente,
        UsuarioResponseDTO usuario
) {
    public static ReservaResponseDTO from(Reserva reserva) {
        return new ReservaResponseDTO(
                reserva.getId(),
                reserva.getProfessor(),
                reserva.getData(),
                reserva.getHorario(),
                reserva.getStatus(),
                AmbienteResponseDTO.from(reserva.getAmbiente()),
                UsuarioResponseDTO.from(reserva.getUsuario())
        );
    }
}
