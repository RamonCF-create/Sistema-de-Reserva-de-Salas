package com.crud.reserva.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ambiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private Integer capacidade;

    @Column(length = 1000)
    private String fichaTecnica;

    @Column(nullable = false)
    private boolean excluido = false;
}
