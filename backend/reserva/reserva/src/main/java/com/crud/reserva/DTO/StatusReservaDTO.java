package com.crud.reserva.DTO;

import jakarta.validation.constraints.NotBlank;

public class StatusReservaDTO {

    @NotBlank(message = "Status e obrigatorio")
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
