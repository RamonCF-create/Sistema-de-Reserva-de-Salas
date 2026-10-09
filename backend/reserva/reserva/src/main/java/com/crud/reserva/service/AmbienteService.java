package com.crud.reserva.service;

import com.crud.reserva.DTO.AmbienteDisponibilidadeDTO;
import com.crud.reserva.model.Ambiente;
import com.crud.reserva.repository.AmbienteRepository;
import com.crud.reserva.repository.ReservaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AmbienteService {

    private final AmbienteRepository repository;

    private final ReservaRepository reservaRepository;

    public Ambiente salvar(Ambiente ambiente) {
        return repository.save(ambiente);
    }

    public List<Ambiente> listar() {
        return repository.findByExcluidoFalse();
    }

    public Ambiente atualizar(Long id, Ambiente ambiente) {
        Ambiente ambienteExistente = repository.findByIdAndExcluidoFalse(id)
                .orElseThrow(() -> new RuntimeException("Ambiente não encontrado"));

        ambienteExistente.setNome(ambiente.getNome());
        ambienteExistente.setCapacidade(ambiente.getCapacidade());
        ambienteExistente.setFichaTecnica(ambiente.getFichaTecnica());

        return repository.save(ambienteExistente);
    }

    public void deletar(Long id) {
        Ambiente ambiente = repository.findByIdAndExcluidoFalse(id)
                .orElseThrow(() -> new RuntimeException("Ambiente não encontrado"));

        ambiente.setExcluido(true);
        repository.save(ambiente);
    }

    public List<AmbienteDisponibilidadeDTO> listarDisponibilidade(
            String data,
            String horarioInicio,
            String horarioFim
    ) {
        LocalDate.parse(data);
        validarHorarios(horarioInicio, horarioFim);

        return repository.findByExcluidoFalse()
                .stream()
                .map(ambiente -> toDisponibilidade(
                        ambiente,
                        data,
                        horarioInicio,
                        horarioFim
                ))
                .toList();
    }

    private AmbienteDisponibilidadeDTO toDisponibilidade(
            Ambiente ambiente,
            String data,
            String horarioInicio,
            String horarioFim
    ) {
        LocalTime inicio = LocalTime.parse(horarioInicio);
        LocalTime fim = LocalTime.parse(horarioFim);

        LocalTime inicioComIntervalo = inicio.minusMinutes(15);
        LocalTime fimComIntervalo = fim.plusMinutes(15);

        boolean ocupado =
                reservaRepository.existsConflitoDeIntervalo(
                        null,
                        ambiente.getId(),
                        data,
                        inicioComIntervalo.toString(),
                        fimComIntervalo.toString(),
                        List.of("PENDENTE", "APROVADA")
                );

        return new AmbienteDisponibilidadeDTO(
                ambiente.getId(),
                ambiente.getNome(),
                ambiente.getCapacidade(),
                ambiente.getFichaTecnica(),
                data,
                horarioInicio,
                horarioFim,
                !ocupado
        );
    }

    private void validarHorarios(String horarioInicio, String horarioFim) {
        if (horarioInicio == null || horarioInicio.isBlank()) {
            throw new RuntimeException("Horário inicial é obrigatório");
        }

        if (horarioFim == null || horarioFim.isBlank()) {
            throw new RuntimeException("Horário final é obrigatório");
        }

        LocalTime inicio = LocalTime.parse(horarioInicio);
        LocalTime fim = LocalTime.parse(horarioFim);

        if (!fim.isAfter(inicio)) {
            throw new RuntimeException("Horário final deve ser depois do horário inicial");
        }
    }
}