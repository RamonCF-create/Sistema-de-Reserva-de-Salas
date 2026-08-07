package com.crud.reserva.DTO;

public record AuthResponse(

        String token,

        Long id,

        String email,

        String role

) {
}