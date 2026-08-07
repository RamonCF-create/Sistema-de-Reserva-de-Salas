package com.crud.reserva.service;

import com.crud.reserva.DTO.AmbienteDisponibilidadeDTO;
import com.crud.reserva.model.Ambiente;
import com.crud.reserva.repository.AmbienteRepository;
import com.crud.reserva.repository.ReservaRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
                .orElseThrow(() -> new RuntimeException("Ambiente nao encontrado"));

        ambienteExistente.setNome(ambiente.getNome());
        ambienteExistente.setCapacidade(ambiente.getCapacidade());
        ambienteExistente.setFichaTecnica(ambiente.getFichaTecnica());

        return repository.save(ambienteExistente);
    }

    public void deletar(Long id) {
        Ambiente ambiente = repository.findByIdAndExcluidoFalse(id)
                .orElseThrow(() -> new RuntimeException("Ambiente nao encontrado"));

        ambiente.setExcluido(true);
        repository.save(ambiente);
    }

    public List<AmbienteDisponibilidadeDTO> listarDisponibilidade(
            String data,
            String horario
    ) {
        LocalDate.parse(data);

        if (horario == null || horario.isBlank()) {
            throw new RuntimeException("Horario e obrigatorio");
        }

        return repository.findByExcluidoFalse()
                .stream()
                .map(ambiente -> toDisponibilidade(ambiente, data, horario))
                .toList();
    }

    private AmbienteDisponibilidadeDTO toDisponibilidade(
            Ambiente ambiente,
            String data,
            String horario
    ) {
        boolean ocupado =
                reservaRepository.existsByAmbienteIdAndDataAndHorarioAndStatusIn(
                        ambiente.getId(),
                        data,
                        horario,
                        List.of("PENDENTE", "APROVADA")
                );

        return new AmbienteDisponibilidadeDTO(
                ambiente.getId(),
                ambiente.getNome(),
                ambiente.getCapacidade(),
                ambiente.getFichaTecnica(),
                data,
                horario,
                !ocupado
        );
    }
}
