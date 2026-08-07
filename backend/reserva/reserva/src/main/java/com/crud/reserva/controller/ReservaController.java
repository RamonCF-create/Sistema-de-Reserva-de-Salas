package com.crud.reserva.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.crud.reserva.DTO.ReservaRequestDTO;
import com.crud.reserva.DTO.ReservaResponseDTO;
import com.crud.reserva.DTO.StatusReservaDTO;
import com.crud.reserva.exception.ReservasNaoEncontradasException;
import com.crud.reserva.model.Ambiente;
import com.crud.reserva.model.Reserva;
import com.crud.reserva.model.Role;
import com.crud.reserva.model.Usuario;
import com.crud.reserva.repository.AmbienteRepository;
import com.crud.reserva.repository.UsuarioRepository;
import com.crud.reserva.service.PdfService;
import com.crud.reserva.service.ReservaService;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
@CrossOrigin("*")
@Validated
@Tag(name = "Reservas", description = "Solicitacao, aprovacao e relatorio de reservas")
@SecurityRequirement(name = "bearerAuth")
public class ReservaController {

    private final ReservaService service;

    private final PdfService pdfService;

    private final AmbienteRepository ambienteRepository;

    private final UsuarioRepository usuarioRepository;

    @PostMapping
    @Operation(summary = "Solicitar reserva")
    public ReservaResponseDTO salvar(
            @Valid @RequestBody ReservaRequestDTO dto,
            Principal principal
    ) {
        Usuario usuario = buscarUsuarioLogado(principal);
        Ambiente ambiente = buscarAmbiente(dto.getAmbienteId());

        Reserva reserva = new Reserva();
        reserva.setAmbiente(ambiente);
        reserva.setUsuario(usuario);
        reserva.setProfessor(dto.getProfessor());
        reserva.setData(dto.getData());
        reserva.setHorarioInicio(dto.getHorarioInicio());
        reserva.setHorarioFim(dto.getHorarioFim());

        return ReservaResponseDTO.from(service.salvar(reserva));
    }

    @GetMapping
    @Operation(summary = "Listar reservas")
    public List<ReservaResponseDTO> listar() {
        return service.listar()
                .stream()
                .map(ReservaResponseDTO::from)
                .toList();
    }

    @GetMapping("/pdf")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Gerar mapa diario de reservas em PDF")
    public ResponseEntity<byte[]> gerarMapaDiarioPdf(
            @RequestParam String data
    ) {
        LocalDate dataConsulta = LocalDate.parse(data);
        List<Reserva> reservasDoDia = service.listarReservasDoDia(data);

        if (reservasDoDia.isEmpty()) {
            throw new ReservasNaoEncontradasException(
                    "Nenhuma reserva encontrada para a data selecionada."
            );
        }

        byte[] pdf = pdfService.gerarMapaDiarioReservas(reservasDoDia, dataConsulta);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename("mapa-diario-reservas-" + data + ".pdf")
                                .build()
                                .toString()
                )
                .body(pdf);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar reserva")
    public ReservaResponseDTO atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ReservaRequestDTO dto,
            Principal principal
    ) {
        Usuario usuario = buscarUsuarioLogado(principal);
        Reserva reservaExistente = service.buscarPorId(id);

        if (usuario.getRole() != Role.ADMIN
                && !reservaExistente.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("Voce nao pode editar esta reserva");
        }

        Reserva reserva = new Reserva();
        reserva.setAmbiente(buscarAmbiente(dto.getAmbienteId()));
        reserva.setProfessor(dto.getProfessor());
        reserva.setData(dto.getData());
        reserva.setHorarioInicio(dto.getHorarioInicio());
        reserva.setHorarioFim(dto.getHorarioFim());

        return ReservaResponseDTO.from(service.atualizar(id, reserva));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aprovar ou rejeitar reserva")
    public ReservaResponseDTO atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusReservaDTO dto
    ) {
        return ReservaResponseDTO.from(service.atualizarStatus(id, dto.getStatus()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir reserva")
    public void deletar(@PathVariable Long id) {
        service.deletar(id);
    }

    private Ambiente buscarAmbiente(Long ambienteId) {
        return ambienteRepository
                .findByIdAndExcluidoFalse(ambienteId)
                .orElseThrow(() -> new RuntimeException("Ambiente nao encontrado"));
    }

    private Usuario buscarUsuarioLogado(Principal principal) {
        return usuarioRepository
                .findByEmailAndExcluidoFalse(principal.getName())
                .orElseThrow(() -> new RuntimeException("Usuario nao encontrado"));
    }
}
