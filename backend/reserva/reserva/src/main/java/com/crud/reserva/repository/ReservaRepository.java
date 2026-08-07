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
            "ORDER BY r.data ASC, r.horario ASC, a.nome ASC")
    List<Reserva> findAll();

    List<Reserva> findByUsuarioIdAndExcluidoFalse(Long usuarioId);

    java.util.Optional<Reserva> findByIdAndExcluidoFalse(Long id);

    @Query("SELECT r FROM Reserva r " +
            "LEFT JOIN FETCH r.ambiente " +
            "WHERE r.data = :data " +
            "AND r.excluido = false " +
            "ORDER BY r.ambiente.nome ASC, r.horario ASC")
    List<Reserva> findReservasDoDiaOrdenadas(@Param("data") String data);

    boolean existsByUsuarioIdAndExcluidoFalse(Long usuarioId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Reserva r " +
            "WHERE r.ambiente.id = :ambienteId " +
            "AND r.data = :data " +
            "AND r.horario = :horario " +
            "AND r.status IN :status " +
            "AND r.excluido = false")
    boolean existsByAmbienteIdAndDataAndHorarioAndStatusIn(
            @Param("ambienteId") Long ambienteId,
            @Param("data") String data,
            @Param("horario") String horario,
            @Param("status") List<String> status
    );

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM Reserva r " +
            "WHERE r.id <> :id " +
            "AND r.ambiente.id = :ambienteId " +
            "AND r.data = :data " +
            "AND r.horario = :horario " +
            "AND r.status IN :status " +
            "AND r.excluido = false")
    boolean existsConflitoDeHorario(
            @Param("id") Long id,
            @Param("ambienteId") Long ambienteId,
            @Param("data") String data,
            @Param("horario") String horario,
            @Param("status") List<String> status
    );
}
