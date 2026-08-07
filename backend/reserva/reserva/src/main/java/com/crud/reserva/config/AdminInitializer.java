package com.crud.reserva.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.crud.reserva.model.Role;
import com.crud.reserva.model.Usuario;
import com.crud.reserva.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        boolean existeAdmin = !usuarioRepository.findByRoleAndExcluidoFalse(Role.ADMIN).isEmpty();

        if (existeAdmin) {
            return;
        }

        Usuario admin = new Usuario(
                "admin@reserva.com",
                passwordEncoder.encode("admin123"),
                Role.ADMIN
        );

        usuarioRepository.save(admin);
    }
}
