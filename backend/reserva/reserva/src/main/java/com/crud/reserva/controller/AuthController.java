package com.crud.reserva.controller;

import com.crud.reserva.DTO.AuthResponse;
import com.crud.reserva.DTO.LoginDTO;
import com.crud.reserva.DTO.MessageResponseDTO;
import com.crud.reserva.DTO.RegisterDTO;
import com.crud.reserva.model.Role;
import com.crud.reserva.model.Usuario;
import com.crud.reserva.repository.UsuarioRepository;
import com.crud.reserva.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacao", description = "Cadastro e login com JWT")
public class AuthController {

    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthController(
            UsuarioRepository repository,
            PasswordEncoder encoder,
            JwtService jwtService
    ) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario")
    public MessageResponseDTO register(
            @Valid @RequestBody RegisterDTO dto
    ) {

        if (repository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Usuario ja existe"
            );
        }

        Usuario usuario = new Usuario(
                dto.getEmail(),
                encoder.encode(dto.getSenha()),
                Role.USER
        );

        repository.save(usuario);

        return new MessageResponseDTO("Usuario criado com sucesso");
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario")
    public AuthResponse login(
            @Valid @RequestBody LoginDTO dto
    ) {

        Usuario usuario = repository.findByEmailAndExcluidoFalse(
                dto.getEmail()
        ).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario nao encontrado"
                )
        );

        boolean senhaCorreta = encoder.matches(
                dto.getSenha(),
                usuario.getSenha()
        );

        if (!senhaCorreta) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Senha invalida"
            );
        }

        String token = jwtService.gerarToken(
                usuario.getEmail(),
                usuario.getRole().name()
        );

        return new AuthResponse(
                token,
                usuario.getId(),
                usuario.getEmail(),
                usuario.getRole().name()
        );
    }
}
