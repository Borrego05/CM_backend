package com.controlmezcla.backend.dto;

import lombok.Data;

@Data
public class ActaSiloRequest {

    private String contacto;
    private String cliente;
    private String cedula;
    private String ciudad_cedula;
    private String ciudad;
    private String obra;
    private String numero_silo;
    private String numero_toneladas;
    private String descripcion;
    private String materiales_utilizados;
    private String nombre_tecnico;
    private String cedula_tecnico;
    private String tipo_mantenimiento;
    private String clase_mantenimiento;
    private Long tecnico_id;
    private String telefono_tecnico;
    private String nombre_recibe;
    private String cedula_recibe;
    private Integer calificacion;
    private String comentario_calificacion;

}
