package com.controlmezcla.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
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
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    private String nombre_tecnico;
    private String cedula_tecnico;
    private String telefono_tecnico;
    private String nombre_recibe;
    private String cedula_recibe;
    private Integer calificacion;
    private String comentario_calificacion;
    private LocalDate fecha;
    private String codigo_acta;
    private String tipo_mantenimiento;
    private String clase_mantenimiento;
    private String firma_tecnico;

    @Column(name = "firma_cliente")
    private String firma_cliente;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime created_at;

    @ManyToOne
    @JoinColumn(name = "fk_tecnico_id")
    private Usuario tecnico;
}
