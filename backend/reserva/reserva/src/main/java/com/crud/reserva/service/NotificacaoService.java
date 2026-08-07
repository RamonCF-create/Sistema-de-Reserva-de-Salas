package com.crud.reserva.service;

import com.crud.reserva.model.Notificacao;
import com.crud.reserva.model.Usuario;
import com.crud.reserva.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository repository;

    public void criarNotificacao(
            Usuario usuario,
            String mensagem
    ) {

        if (usuario == null) {
            return;
        }

        Notificacao notificacao =
                Notificacao.builder()
                        .usuario(usuario)
                        .mensagem(mensagem)
                        .dataHora(LocalDateTime.now())
                        .lida(false)
                        .build();

        repository.save(notificacao);
    }

    public List<Notificacao> listarPorUsuario(
            Long usuarioId
    ) {

        return repository
                .findByUsuarioIdOrderByDataHoraDesc(
                        usuarioId
                );
    }

    public void marcarComoLida(
            Long notificacaoId,
            Long usuarioId
    ) {

        Notificacao notificacao =
                repository.findByIdAndUsuarioId(notificacaoId, usuarioId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Notificacao nao encontrada"
                                )
                        );

        notificacao.setLida(true);

        repository.save(notificacao);
    }
}
