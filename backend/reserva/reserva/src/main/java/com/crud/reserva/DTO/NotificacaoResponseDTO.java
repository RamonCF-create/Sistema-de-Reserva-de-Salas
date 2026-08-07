package com.crud.reserva.DTO;

import com.crud.reserva.model.Notificacao;

import java.time.LocalDateTime;

public record NotificacaoResponseDTO(
        Long id,
        String mensagem,
        LocalDateTime dataHora,
        boolean lida
) {
    public static NotificacaoResponseDTO from(Notificacao notificacao) {
        return new NotificacaoResponseDTO(
                notificacao.getId(),
                notificacao.getMensagem(),
                notificacao.getDataHora(),
                notificacao.isLida()
        );
    }
}
