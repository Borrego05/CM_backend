package com.controlmezcla.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "formulario")

public class Formulario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cliente;
    private String direccion;
    private String obra;
    private String telefono;
    private LocalDate fecha;
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private String firma_cliente;
    private String firma_tecnico;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico_id;
}
