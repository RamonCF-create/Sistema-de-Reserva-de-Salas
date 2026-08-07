package com.crud.reserva.repository;

import com.crud.reserva.model.Usuario;
import com.crud.reserva.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailAndExcluidoFalse(String email);

    Optional<Usuario> findByIdAndExcluidoFalse(Long id);

    List<Usuario> findByRoleAndExcluidoFalse(Role role);

    List<Usuario> findByExcluidoFalse();

}
