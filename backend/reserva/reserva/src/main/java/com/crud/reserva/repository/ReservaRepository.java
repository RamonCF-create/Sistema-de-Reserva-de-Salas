package com.crud.reserva.repository;

import com.crud.reserva.model.Reserva;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Override
    @EntityGraph(attributePaths = {"ambiente", "usuario"})
    @Query("SELECT r FROM Reserva r " +
            "LEFT JOIN r.ambiente a " +
            "WHERE r.excluido = false " +
            "ORDER BY r.data ASC, r.horarioInicio ASC, a.nome ASC")
    List<Reserva> findAll();

    List<Reserva> findByUsuarioIdAndExcluidoFalse(Long usuarioId);

    java.util.Optional<Reserva> findByIdAndExcluidoFalse(Long id);

    @Query("SELECT r FROM Reserva r " +
            "LEFT JOIN FETCH r.ambiente " +
            "WHERE r.data = :data " +
            "AND r.excluido = false " +
            "ORDER BY r.ambiente.nome ASC, r.horarioInicio ASC")
    List<Reserva> findReservasDoDiaOrdenadas(@Param("data") String data);

    boolean existsByUsuarioIdAndExcluidoFalse(Long usuarioId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Reserva r " +
            "WHERE r.ambiente.id = :ambienteId " +
            "AND r.data = :data " +
            "AND r.status IN :status " +
            "AND r.excluido = false " +
            "AND (:reservaId IS NULL OR r.id <> :reservaId) " +
            "AND r.horarioInicio < :horarioFimComIntervalo " +
            "AND r.horarioFim > :horarioInicioComIntervalo")
    boolean existsConflitoDeIntervalo(
            @Param("reservaId") Long reservaId,
            @Param("ambienteId") Long ambienteId,
            @Param("data") String data,
            @Param("horarioInicioComIntervalo") String horarioInicioComIntervalo,
            @Param("horarioFimComIntervalo") String horarioFimComIntervalo,
            @Param("status") List<String> status
    );
}