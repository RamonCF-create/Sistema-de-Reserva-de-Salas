package com.crud.reserva.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.crud.reserva.DTO.NotificacaoResponseDTO;
import com.crud.reserva.model.Notificacao;
import com.crud.reserva.model.Usuario;
import com.crud.reserva.repository.UsuarioRepository;
import com.crud.reserva.service.NotificacaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notificacoes")
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "Notificações", description = "Notificações do usuário autenticado")
@SecurityRequirement(name = "bearerAuth")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @Operation(summary = "Listar notificações do usuário logado")
    public List<NotificacaoResponseDTO> listar(Principal principal) {
        Usuario usuario = usuarioRepository
                .findByEmailAndExcluidoFalse(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuário nao encontrado"));

        return notificacaoService.listarPorUsuario(usuario.getId())
                .stream()
                .map(NotificacaoResponseDTO::from)
                .toList();
    }

    @PatchMapping("/{id}/lida")
    @Operation(summary = "Marcar notificação como lida")
    public void marcarComoLida(
            @PathVariable Long id,
            Principal principal
    ) {

        Usuario usuario = usuarioRepository
                .findByEmailAndExcluidoFalse(principal.getName())
                .orElseThrow(() ->
                        new RuntimeException("Usuário não encontrado")
                );

        notificacaoService.marcarComoLida(
                id,
                usuario.getId()
        );
    }
}
