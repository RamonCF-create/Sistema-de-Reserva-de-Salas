package com.crud.reserva.DTO;

import com.crud.reserva.model.Usuario;

public record UsuarioResponseDTO(
        Long id,
        String email,
        String role
) {

    public static UsuarioResponseDTO from(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRole().name()
        );
    }
}
