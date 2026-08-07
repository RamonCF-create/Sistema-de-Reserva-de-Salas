package com.crud.reserva.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mensagem;

    private LocalDateTime dataHora;

    private boolean lida;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "usuario_id")

    @JsonIgnore
    private Usuario usuario;
}