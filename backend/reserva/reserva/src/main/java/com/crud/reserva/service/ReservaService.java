package com.crud.reserva.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.crud.reserva.model.Reserva;
import com.crud.reserva.model.Role;
import com.crud.reserva.repository.ReservaRepository;
import com.crud.reserva.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository repository;

    private final UsuarioRepository usuarioRepository;

    private final NotificacaoService notificacaoService;

    public Reserva salvar(Reserva reserva) {

        validarDataNaoPassada(reserva.getData());

        validarDisponibilidade(reserva, null);

        reserva.setStatus("PENDENTE");

        Reserva novaReserva = repository.save(reserva);

        notificacaoService.criarNotificacao(
                novaReserva.getUsuario(),
                "Sua reserva foi solicitada com sucesso."
        );

        usuarioRepository.findByRoleAndExcluidoFalse(Role.ADMIN)
                .forEach(admin -> notificacaoService.criarNotificacao(
                        admin,
                        "Nova reserva pendente para aprovação: " + descricaoReserva(novaReserva)
                ));

        return novaReserva;
    }

    public List<Reserva> listar() {
        return repository.findAll();
    }

    public List<Reserva> listarPorUsuario(Long usuarioId) {
        return repository.findByUsuarioIdAndExcluidoFalse(usuarioId);
    }

    public List<Reserva> listarReservasDoDia(String data) {
        LocalDate.parse(data);
        return repository.findReservasDoDiaOrdenadas(data);
    }

    public Reserva atualizar(Long id, Reserva reserva) {

        validarDataNaoPassada(reserva.getData());

        Reserva reservaExistente = buscarPorId(id);

        validarDisponibilidade(reserva, id);

        reservaExistente.setAmbiente(reserva.getAmbiente());
        reservaExistente.setProfessor(reserva.getProfessor());
        reservaExistente.setData(reserva.getData());
        reservaExistente.setHorarioInicio(reserva.getHorarioInicio());
        reservaExistente.setHorarioFim(reserva.getHorarioFim());

        return repository.save(reservaExistente);
    }

    public Reserva atualizarStatus(Long id, String novoStatus) {
        Reserva reserva = buscarPorId(id);
        String statusAnterior = reserva.getStatus();

        reserva.setStatus(validarStatus(novoStatus));

        Reserva reservaAtualizada = repository.save(reserva);

        if (!reservaAtualizada.getStatus().equals(statusAnterior)) {
            criarNotificacaoDeStatus(reservaAtualizada);
        }

        return reservaAtualizada;
    }

    public void deletar(Long id) {
        Reserva reserva = buscarPorId(id);

        notificacaoService.criarNotificacao(
                reserva.getUsuario(),
                "Sua reserva foi excluída."
        );

        reserva.setExcluido(true);
        repository.save(reserva);
    }

    public Reserva buscarPorId(Long id) {
        return repository.findByIdAndExcluidoFalse(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
    }

    private void validarDisponibilidade(Reserva reserva, Long reservaId) {
        validarHorarios(reserva);

        LocalTime inicio = LocalTime.parse(reserva.getHorarioInicio());
        LocalTime fim = LocalTime.parse(reserva.getHorarioFim());

        LocalTime inicioComIntervalo = inicio.minusMinutes(15);
        LocalTime fimComIntervalo = fim.plusMinutes(15);

        boolean ocupado = repository.existsConflitoDeIntervalo(
                reservaId,
                reserva.getAmbiente().getId(),
                reserva.getData(),
                inicioComIntervalo.toString(),
                fimComIntervalo.toString(),
                List.of("PENDENTE", "APROVADA")
        );

        if (ocupado) {
            throw new RuntimeException(
                    "Já existe uma reserva para este ambiente neste periodo ou no intervalo mínimo de 15 minutos"
            );
        }
    }


    private void validarHorarios(Reserva reserva) {
        if (reserva.getHorarioInicio() == null
                || reserva.getHorarioInicio().isBlank()) {
            throw new RuntimeException("Horário inicial é obrigatório");
        }

        if (reserva.getHorarioFim() == null
                || reserva.getHorarioFim().isBlank()) {
            throw new RuntimeException("Horário final é obrigatório");
        }

        LocalTime inicio = LocalTime.parse(reserva.getHorarioInicio());
        LocalTime fim = LocalTime.parse(reserva.getHorarioFim());

        if (!fim.isAfter(inicio)) {
            throw new RuntimeException(
                    "Horário final deve ser depois do horário inicial"
            );
        }

        long duracaoMinutos = java.time.Duration
                .between(inicio, fim)
                .toMinutes();

        if (duracaoMinutos < 15) {
            throw new RuntimeException(
                    "A reserva deve ter duração mínima de 15 minutos."
            );
        }
    }


    private String validarStatus(String status) {

        if ("APROVADA".equalsIgnoreCase(status)) {
            return "APROVADA";
        }

        if ("REJEITADA".equalsIgnoreCase(status)) {
            return "REJEITADA";
        }

        throw new RuntimeException(
                "Status inválido. Apenas APROVADA ou REJEITADA são permitidos."
        );
    }

    private void criarNotificacaoDeStatus(Reserva reserva) {

        if ("APROVADA".equals(reserva.getStatus())) {

            notificacaoService.criarNotificacao(
                    reserva.getUsuario(),
                    "Sua reserva foi aprovada."
            );

            return;
        }

        if ("REJEITADA".equals(reserva.getStatus())) {

            notificacaoService.criarNotificacao(
                    reserva.getUsuario(),
                    "Sua reserva foi rejeitada."
            );
        }
    }

    private String descricaoReserva(Reserva reserva) {

        String ambiente =
                reserva.getAmbiente() != null
                        ? reserva.getAmbiente().getNome()
                        : "ambiente";

        return ambiente
                + " em "
                + reserva.getData()
                + " das "
                + reserva.getHorarioInicio()
                + " até "
                + reserva.getHorarioFim();
    }

    private void validarDataNaoPassada(String dataStr) {
        LocalDate data = LocalDate.parse(dataStr);
        LocalDate hoje = LocalDate.now();

        if (data.isBefore(hoje)) {
            throw new RuntimeException("Não é possível reservar em datas passadas");
        }
    }
}