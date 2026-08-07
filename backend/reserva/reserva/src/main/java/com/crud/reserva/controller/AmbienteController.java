package com.crud.reserva.controller;

import com.crud.reserva.DTO.AmbienteRequestDTO;
import com.crud.reserva.DTO.AmbienteResponseDTO;
import com.crud.reserva.DTO.AmbienteDisponibilidadeDTO;
import com.crud.reserva.model.Ambiente;
import com.crud.reserva.service.AmbienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ambientes")
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "Ambientes", description = "CRUD de ambientes reservaveis")
@SecurityRequirement(name = "bearerAuth")
public class AmbienteController {

    private final AmbienteService service;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar ambiente")
    public AmbienteResponseDTO salvar(
            @Valid @RequestBody AmbienteRequestDTO dto
    ) {
        return AmbienteResponseDTO.from(service.salvar(toModel(dto)));
    }

    @GetMapping
    @Operation(summary = "Listar ambientes")
    public List<AmbienteResponseDTO> listar() {
        return service.listar()
                .stream()
                .map(AmbienteResponseDTO::from)
                .toList();
    }

    @GetMapping("/disponibilidade")
    @Operation(summary = "Consultar disponibilidade dos ambientes por data e horario")
    public List<AmbienteDisponibilidadeDTO> listarDisponibilidade(
            @RequestParam String data,
            @RequestParam String horarioInicio,
            @RequestParam String horarioFim
    ) {
        return service.listarDisponibilidade(data, horarioInicio, horarioFim);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar ambiente")
    public AmbienteResponseDTO atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AmbienteRequestDTO dto
    ) {
        return AmbienteResponseDTO.from(service.atualizar(id, toModel(dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir ambiente")
    public void deletar(
            @PathVariable Long id
    ) {
        service.deletar(id);
    }

    private Ambiente toModel(AmbienteRequestDTO dto) {
        Ambiente ambiente = new Ambiente();
        ambiente.setNome(dto.getNome());
        ambiente.setCapacidade(dto.getCapacidade());
        ambiente.setFichaTecnica(dto.getFichaTecnica());
        return ambiente;
    }
}
