package com.crud.reserva.repository;

import com.crud.reserva.model.Ambiente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AmbienteRepository
        extends JpaRepository<Ambiente, Long> {

    List<Ambiente> findByExcluidoFalse();

    Optional<Ambiente> findByIdAndExcluidoFalse(Long id);
}
