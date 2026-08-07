package com.crud.reserva.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AmbienteRequestDTO {

    @NotBlank(message = "Nome do ambiente e obrigatorio")
    private String nome;

    @NotNull(message = "Capacidade e obrigatoria")
    @Min(value = 1, message = "Capacidade deve ser maior que zero")
    private Integer capacidade;

    private String fichaTecnica;
}
