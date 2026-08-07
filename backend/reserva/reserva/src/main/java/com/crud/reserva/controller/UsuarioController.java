package com.crud.reserva.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.crud.reserva.DTO.UsuarioResponseDTO;
import com.crud.reserva.DTO.UsuarioRoleDTO;
import com.crud.reserva.model.Role;
import com.crud.reserva.model.Usuario;
import com.crud.reserva.repository.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "Usuarios", description = "Administracao de usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios")
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findByExcluidoFalse()
                .stream()
                .map(UsuarioResponseDTO::from)
                .toList();
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Alterar perfil do usuario")
    public UsuarioResponseDTO atualizarRole(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioRoleDTO dto,
            Principal principal
    ) {
        Usuario usuarioLogado = usuarioRepository.findByEmailAndExcluidoFalse(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario logado nao encontrado"));

        if (usuarioLogado.getId().equals(id)) {
            throw new RuntimeException("Voce nao pode alterar o proprio perfil");
        }

        Usuario usuario = usuarioRepository.findByIdAndExcluidoFalse(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        usuario.setRole(validarRole(dto.getRole()));

        return UsuarioResponseDTO.from(usuarioRepository.save(usuario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir usuario")
    public void deletar(
            @PathVariable Long id,
            Principal principal
    ) {
        Usuario usuarioLogado = usuarioRepository.findByEmailAndExcluidoFalse(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario logado nao encontrado"));

        if (usuarioLogado.getId().equals(id)) {
            throw new RuntimeException("Voce nao pode excluir a propria conta");
        }

        Usuario usuario = usuarioRepository.findByIdAndExcluidoFalse(id)
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));

        usuario.setExcluido(true);
        usuarioRepository.save(usuario);
    }

    private Role validarRole(String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return Role.ADMIN;
        }

        if ("USER".equalsIgnoreCase(role)) {
            return Role.USER;
        }

        throw new RuntimeException("Perfil invalido");
    }
}
