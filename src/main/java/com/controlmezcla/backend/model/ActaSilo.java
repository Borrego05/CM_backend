package com.controlmezcla.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name ="acta_silo")
public class ActaSilo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contacto;
    private String cliente;
    private String cedula;
    private String ciudad_cedula;
    private String ciudad;
    private String obra;
    private String numero_silo;
    private String numero_toneladas;
    private String descripcion;
    private String nombre_tecnico;
    private String cedula_tecnico;
    private String fecha;
    private String codigo_acta;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime created_at;

    @ManyToOne
    @JoinColumn(name = "fk_tecnico_id")
    private Usuario tecnico;
}
