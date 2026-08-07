package com.crud.reserva.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.crud.reserva.model.Reserva;
import com.crud.reserva.model.Role;
import com.crud.reserva.repository.ReservaRepository;
import com.crud.reserva.repository.UsuarioRepository;
import java.time.LocalDate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository repository;

    private final UsuarioRepository usuarioRepository;

    private final NotificacaoService notificacaoService;

    public Reserva salvar(Reserva reserva) {

        validarDataNaoPassada(reserva.getData());

        validarDisponibilidade(reserva);

        reserva.setStatus("PENDENTE");

        Reserva novaReserva = repository.save(reserva);

        notificacaoService.criarNotificacao(
                novaReserva.getUsuario(),
                "Sua reserva foi solicitada com sucesso."
        );

        usuarioRepository.findByRoleAndExcluidoFalse(Role.ADMIN)
                .forEach(admin -> notificacaoService.criarNotificacao(
                        admin,
                        "Nova reserva pendente para aprovacao: " + descricaoReserva(novaReserva)
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

        boolean conflito =
                repository.existsConflitoDeHorario(
                        id,
                        reserva.getAmbiente().getId(),
                        reserva.getData(),
                        reserva.getHorario(),
                        List.of("PENDENTE", "APROVADA")
                );

        if (conflito) {
            throw new RuntimeException(
                    "Ja existe uma reserva para este ambiente neste horario"
            );
        }

        reservaExistente.setAmbiente(reserva.getAmbiente());
        reservaExistente.setProfessor(reserva.getProfessor());
        reservaExistente.setData(reserva.getData());
        reservaExistente.setHorario(reserva.getHorario());

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
                "Sua reserva foi excluida."
        );

        reserva.setExcluido(true);
        repository.save(reserva);
    }

    public Reserva buscarPorId(Long id) {
        return repository.findByIdAndExcluidoFalse(id)
                .orElseThrow(() -> new RuntimeException("Reserva nao encontrada"));
    }

    private String validarStatus(String status) {

        if ("APROVADA".equalsIgnoreCase(status)) {
            return "APROVADA";
        }

        if ("REJEITADA".equalsIgnoreCase(status)) {
            return "REJEITADA";
        }

        throw new RuntimeException(
                "Status invalido. Apenas APROVADA ou REJEITADA sao permitidos."
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
                + " as "
                + reserva.getHorario();
    }

    private void validarDisponibilidade(Reserva reserva) {

        boolean ocupado =
                repository.existsByAmbienteIdAndDataAndHorarioAndStatusIn(
                        reserva.getAmbiente().getId(),
                        reserva.getData(),
                        reserva.getHorario(),
                        List.of("PENDENTE", "APROVADA")
                );

        if (ocupado) {
            throw new RuntimeException(
                    "Ja existe uma reserva para este ambiente neste horario"
            );
        }
    }

    private void validarDataNaoPassada(String dataStr) {
        LocalDate data = LocalDate.parse(dataStr);
        LocalDate hoje = LocalDate.now();
        if (data.isBefore(hoje)) {
            throw new RuntimeException("Nao e possivel reservar em datas passadas");
        }
    }
}
