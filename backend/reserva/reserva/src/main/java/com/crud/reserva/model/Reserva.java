package com.crud.reserva.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String professor;

    private String data;

    private String horarioInicio;

    private String horarioFim;

    private String status;

    @Column(nullable = false)
    private boolean excluido = false;

    @ManyToOne
    @JoinColumn(name = "ambiente_id")
    private Ambiente ambiente;

    @ManyToOne
    private Usuario usuario;
}
