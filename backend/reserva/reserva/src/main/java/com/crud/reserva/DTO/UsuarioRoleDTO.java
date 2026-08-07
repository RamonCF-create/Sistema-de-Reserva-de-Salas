package com.crud.reserva.DTO;

import jakarta.validation.constraints.NotBlank;

public class UsuarioRoleDTO {

    @NotBlank(message = "Perfil e obrigatorio")
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
