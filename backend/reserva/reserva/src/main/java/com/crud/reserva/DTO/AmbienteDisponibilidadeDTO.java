package com.crud.reserva.DTO;

public record AmbienteDisponibilidadeDTO(
        Long id,
        String nome,
        Integer capacidade,
        String fichaTecnica,
        String data,
        String horarioInicio,
        String horarioFim,
        boolean disponivel
) {
}