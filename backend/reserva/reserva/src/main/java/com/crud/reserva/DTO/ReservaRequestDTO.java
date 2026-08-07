package com.crud.reserva.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservaRequestDTO {

    @NotBlank(message = "Professor e obrigatorio")
    private String professor;

    @NotBlank(message = "Data e obrigatoria")
    private String data;

    @NotBlank(message = "Horario inicial e obrigatorio")
    private String horarioInicio;

    @NotBlank(message = "Horario final e obrigatorio")
    private String horarioFim;
    private String status;

    @NotNull(message = "Ambiente e obrigatorio")
    private Long ambienteId;

    private Long usuarioId;
}
